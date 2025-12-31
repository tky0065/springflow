package io.springflow.core.filter;

import io.springflow.annotations.Filterable;
import io.springflow.annotations.FilterType;
import org.junit.jupiter.api.Test;
import java.lang.reflect.Method;
import static org.junit.jupiter.api.Assertions.*;

class FilterableAnnotationTest {

    @Test
    void shouldHaveRequiredAttributes() throws NoSuchMethodException {
        Method types = Filterable.class.getMethod("types");
        assertNotNull(types);
        assertEquals(FilterType[].class, types.getReturnType());
        assertArrayEquals(new FilterType[]{FilterType.EQUALS}, (FilterType[]) types.getDefaultValue());

        Method paramName = Filterable.class.getMethod("paramName");
        assertNotNull(paramName);
        assertEquals(String.class, paramName.getReturnType());
        assertEquals("", paramName.getDefaultValue());

        Method description = Filterable.class.getMethod("description");
        assertNotNull(description);
        assertEquals(String.class, description.getReturnType());
        assertEquals("", description.getDefaultValue());

        Method caseSensitive = Filterable.class.getMethod("caseSensitive");
        assertNotNull(caseSensitive);
        assertEquals(boolean.class, caseSensitive.getReturnType());
        assertEquals(true, caseSensitive.getDefaultValue());
    }
}
