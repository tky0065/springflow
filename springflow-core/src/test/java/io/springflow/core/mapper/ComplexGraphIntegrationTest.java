package io.springflow.core.mapper;

import io.springflow.annotations.AutoApi;
import io.springflow.core.infrastructure.H2IntegrationTest;
import io.springflow.core.metadata.EntityMetadata;
import io.springflow.core.metadata.MetadataResolver;
import jakarta.persistence.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = H2IntegrationTest.TestConfig.class)
@Transactional
class ComplexGraphIntegrationTest extends H2IntegrationTest {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private DtoMapperFactory mapperFactory;

    @Autowired
    private MetadataResolver metadataResolver;

    @Test
    void toOutputDto_manyToMany_shouldHandleBidirectionalMapping() {
        // Given
        Student student1 = new Student();
        student1.setName("Alice");
        
        Student student2 = new Student();
        student2.setName("Bob");

        Course course1 = new Course();
        course1.setTitle("Math");
        
        Course course2 = new Course();
        course2.setTitle("Physics");

        // Establish Many-to-Many
        student1.setCourses(List.of(course1, course2));
        student2.setCourses(List.of(course1));
        
        course1.setStudents(List.of(student1, student2));
        course2.setStudents(List.of(student1));

        entityManager.persist(student1);
        entityManager.persist(student2);
        entityManager.persist(course1);
        entityManager.persist(course2);
        entityManager.flush();
        entityManager.clear();

        // Refresh to ensure we get persistent state
        Student savedStudent = entityManager.find(Student.class, student1.getId());

        EntityMetadata metadata = metadataResolver.resolve(Student.class);
        EntityDtoMapper<Student, Long> mapper = new EntityDtoMapper<>(
                Student.class,
                metadata,
                entityManager,
                mapperFactory,
                2 // Allow some depth
        );

        // When
        Map<String, Object> dto = mapper.toOutputDto(savedStudent);

        // Then
        assertThat(dto).isNotNull();
        assertThat(dto.get("name")).isEqualTo("Alice");
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> courses = (List<Map<String, Object>>) dto.get("courses");
        assertThat(courses).hasSize(2);
        
        // Verify courses are mapped
        assertThat(courses).extracting("title").containsExactlyInAnyOrder("Math", "Physics");
        
        // Verify nested students in courses are mapped (due to depth 2) or summarized
        // Depending on depth calculation, circular ref might trigger here
        // Ideally we want to see that it didn't crash
    }

    @Test
    void toOutputDto_circularRef_shouldNotCrash() {
        // Given A <-> B
        NodeA a = new NodeA();
        a.setName("A");
        
        NodeB b = new NodeB();
        b.setName("B");
        
        a.setB(b);
        b.setA(a);

        entityManager.persist(a);
        entityManager.persist(b);
        entityManager.flush();
        entityManager.clear();

        NodeA savedA = entityManager.find(NodeA.class, a.getId());

        EntityMetadata metadata = metadataResolver.resolve(NodeA.class);
        EntityDtoMapper<NodeA, Long> mapper = new EntityDtoMapper<>(
                NodeA.class,
                metadata,
                entityManager,
                mapperFactory
        );

        // When
        Map<String, Object> dto = mapper.toOutputDto(savedA);

        // Then
        assertThat(dto).isNotNull();
        assertThat(dto.get("name")).isEqualTo("A");
        
        @SuppressWarnings("unchecked")
        Map<String, Object> bDto = (Map<String, Object>) dto.get("b");
        assertThat(bDto).isNotNull();
        assertThat(bDto.get("name")).isEqualTo("B");
        
        // Circular ref check: b -> a should be summary or ID
        Object aRef = bDto.get("a");
        // Depending on implementation, it might be a Map with ID or just ID. 
        // Our recent changes made summary default to Map with ID if no summary fields.
        if (aRef instanceof Map) {
             assertThat(((Map) aRef).get("id")).isEqualTo(savedA.getId());
        } else {
             // Fallback
             assertThat(aRef).isEqualTo(savedA.getId());
        }
    }

    @Entity
    @AutoApi
    static class Student {
        @Id @GeneratedValue private Long id;
        private String name;
        @ManyToMany(cascade = CascadeType.ALL)
        @JoinTable(name = "student_course",
            joinColumns = @JoinColumn(name = "student_id"),
            inverseJoinColumns = @JoinColumn(name = "course_id"))
        private List<Course> courses;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public List<Course> getCourses() { return courses; }
        public void setCourses(List<Course> courses) { this.courses = courses; }
    }

    @Entity
    @AutoApi
    static class Course {
        @Id @GeneratedValue private Long id;
        private String title;
        @ManyToMany(mappedBy = "courses")
        private List<Student> students;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public List<Student> getStudents() { return students; }
        public void setStudents(List<Student> students) { this.students = students; }
    }

    @Entity
    @AutoApi
    static class NodeA {
        @Id @GeneratedValue private Long id;
        private String name;
        @OneToOne(cascade = CascadeType.ALL)
        private NodeB b;
        
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public NodeB getB() { return b; }
        public void setB(NodeB b) { this.b = b; }
    }

    @Entity
    @AutoApi
    static class NodeB {
        @Id @GeneratedValue private Long id;
        private String name;
        @OneToOne(mappedBy = "b")
        private NodeA a;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public NodeA getA() { return a; }
        public void setA(NodeA a) { this.a = a; }
    }
}
