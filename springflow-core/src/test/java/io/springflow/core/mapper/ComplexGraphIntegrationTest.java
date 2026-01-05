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

    private final MetadataResolver metadataResolver = new MetadataResolver();

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
        List<Object> courses = (List<Object>) dto.get("courses");
        assertThat(courses).hasSize(2);
        
        // Since factory creates default mappers with depth 1, the nested Course objects 
        // (depth 1 relative to root) might be mapped as IDs or Summaries depending on context depth check.
        // Context depth starts at 0. Enter Student (1). Map courses. 
        // Course mapper (depth 1). Check: currentDepth(1) >= maxDepth(1) -> True.
        // So returns ID/Summary.
        
        assertThat(courses).allSatisfy(c -> {
             if (c instanceof Map) {
                 assertThat(((Map<?,?>)c).containsKey("id")).isTrue();
             } else {
                 assertThat(c).isInstanceOf(Long.class);
             }
        });
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
        NodeB savedB = entityManager.find(NodeB.class, b.getId());

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
        
        Object bRef = dto.get("b");
        // Expect ID or Summary because NodeA(1) calls NodeB mapper(1). 1>=1 -> ID.
        
        if (bRef instanceof Map) {
             assertThat(((Map<?,?>) bRef).get("id")).isEqualTo(savedB.getId());
        } else {
             assertThat(bRef).isEqualTo(savedB.getId());
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