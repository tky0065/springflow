package io.springflow.core.filter;

import io.springflow.annotations.FilterType;
import io.springflow.annotations.Filterable;
import io.springflow.core.metadata.EntityMetadata;
import io.springflow.core.metadata.FieldMetadata;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Resolves dynamic filters from query parameters into JPA Specifications.
 * <p>
 * This class parses query parameters and maps them to {@link FilterType} operations
 * based on the {@link Filterable} annotations present on entity fields.
 * </p>
 *
 * @author SpringFlow
 * @since 0.1.0
 */
public class FilterResolver {

    private final ConversionService conversionService;

    public FilterResolver() {
        this(DefaultConversionService.getSharedInstance());
    }

    public FilterResolver(ConversionService conversionService) {
        this.conversionService = conversionService;
    }

    /**
     * Builds a JPA Specification from query parameters and entity metadata.
     *
     * @param params   the query parameters
     * @param metadata the entity metadata
     * @param <T>      the entity type
     * @return a Specification that can be used with JpaSpecificationExecutor
     */
    public <T> Specification<T> buildSpecification(Map<String, String> params, EntityMetadata metadata) {
        return buildSpecification(params, metadata, null);
    }

    /**
     * Builds a JPA Specification from query parameters and entity metadata, with support for fetch joins.
     *
     * @param params      the query parameters
     * @param metadata    the entity metadata
     * @param fetchFields list of relation fields to fetch eagerly
     * @param <T>         the entity type
     * @return a Specification that can be used with JpaSpecificationExecutor
     */
    public <T> Specification<T> buildSpecification(Map<String, String> params, EntityMetadata metadata, List<String> fetchFields) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Add fetch joins if not a count query
            if (query.getResultType() != Long.class && query.getResultType() != long.class) {
                if (fetchFields != null && !fetchFields.isEmpty()) {
                    for (String fetchField : fetchFields) {
                        metadata.getFieldByName(fetchField)
                                .filter(FieldMetadata::isRelation)
                                .ifPresent(fm -> root.fetch(fm.name(), jakarta.persistence.criteria.JoinType.LEFT));
                    }
                    query.distinct(true);
                } else {
                    // Default: fetch all non-hidden relations to avoid N+1 for first level depth
                    for (FieldMetadata fieldMetadata : metadata.fields()) {
                        if (fieldMetadata.isRelation() && !fieldMetadata.hidden()) {
                            root.fetch(fieldMetadata.name(), jakarta.persistence.criteria.JoinType.LEFT);
                        }
                    }
                    query.distinct(true);
                }
            }

            for (FieldMetadata fieldMetadata : metadata.fields()) {
                if (fieldMetadata.filterConfig() != null) {
                    predicates.addAll(buildPredicatesForField(root, cb, fieldMetadata, params));
                }
            }

            return predicates.isEmpty() ? cb.conjunction() : cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private List<Predicate> buildPredicatesForField(Root<?> root, CriteriaBuilder cb, FieldMetadata fieldMetadata, Map<String, String> params) {
        List<Predicate> predicates = new ArrayList<>();
        Filterable config = fieldMetadata.filterConfig();
        String baseParamName = StringUtils.hasText(config.paramName()) ? config.paramName() : fieldMetadata.name();
        List<FilterType> supportedTypes = Arrays.asList(config.types());

        // EQUALS (?field=value)
        if (supportedTypes.contains(FilterType.EQUALS) && params.containsKey(baseParamName)) {
            String value = params.get(baseParamName);
            predicates.add(buildEqualsPredicate(root, cb, fieldMetadata, value, config.caseSensitive()));
        }

        // LIKE (?field_like=value)
        if (supportedTypes.contains(FilterType.LIKE) && params.containsKey(baseParamName + "_like")) {
            String value = params.get(baseParamName + "_like");
            predicates.add(buildLikePredicate(root, cb, fieldMetadata, value, config.caseSensitive()));
        }

        // GREATER_THAN (?field_gt=value)
        if (supportedTypes.contains(FilterType.GREATER_THAN) && params.containsKey(baseParamName + "_gt")) {
            String value = params.get(baseParamName + "_gt");
            predicates.add(buildGreaterThanPredicate(root, cb, fieldMetadata, value));
        }

        // GREATER_THAN_OR_EQUAL (?field_gte=value)
        if (supportedTypes.contains(FilterType.GREATER_THAN_OR_EQUAL) || supportedTypes.contains(FilterType.RANGE)) {
             if (params.containsKey(baseParamName + "_gte")) {
                 String value = params.get(baseParamName + "_gte");
                 predicates.add(buildGreaterThanOrEqualPredicate(root, cb, fieldMetadata, value));
             }
        }

        // LESS_THAN (?field_lt=value)
        if (supportedTypes.contains(FilterType.LESS_THAN) && params.containsKey(baseParamName + "_lt")) {
            String value = params.get(baseParamName + "_lt");
            predicates.add(buildLessThanPredicate(root, cb, fieldMetadata, value));
        }

        // LESS_THAN_OR_EQUAL (?field_lte=value)
        if (supportedTypes.contains(FilterType.LESS_THAN_OR_EQUAL) || supportedTypes.contains(FilterType.RANGE)) {
            if (params.containsKey(baseParamName + "_lte")) {
                String value = params.get(baseParamName + "_lte");
                predicates.add(buildLessThanOrEqualPredicate(root, cb, fieldMetadata, value));
            }
        }

        // IN (?field_in=v1,v2,v3)
        if (supportedTypes.contains(FilterType.IN) && params.containsKey(baseParamName + "_in")) {
            String value = params.get(baseParamName + "_in");
            predicates.add(buildInPredicate(root, cb, fieldMetadata, value));
        }

        // NOT_IN (?field_not_in=v1,v2,v3)
        if (supportedTypes.contains(FilterType.NOT_IN) && params.containsKey(baseParamName + "_not_in")) {
            String value = params.get(baseParamName + "_not_in");
            predicates.add(cb.not(buildInPredicate(root, cb, fieldMetadata, value)));
        }

        // IS_NULL (?field_null=true/false)
        if (supportedTypes.contains(FilterType.IS_NULL) && params.containsKey(baseParamName + "_null")) {
            boolean isNull = Boolean.parseBoolean(params.get(baseParamName + "_null"));
            predicates.add(isNull ? cb.isNull(root.get(fieldMetadata.name())) : cb.isNotNull(root.get(fieldMetadata.name())));
        }

        // BETWEEN (?field_between=min,max)
        if (supportedTypes.contains(FilterType.BETWEEN) && params.containsKey(baseParamName + "_between")) {
            String value = params.get(baseParamName + "_between");
            String[] parts = value.split(",");
            if (parts.length == 2) {
                predicates.add(buildBetweenPredicate(root, cb, fieldMetadata, parts[0], parts[1]));
            }
        }

        return predicates;
    }

    private Predicate buildEqualsPredicate(Root<?> root, CriteriaBuilder cb, FieldMetadata fieldMetadata, String value, boolean caseSensitive) {
        Path<Object> path = root.get(fieldMetadata.name());
        Object convertedValue = convert(value, fieldMetadata.type());

        if (fieldMetadata.type() == String.class && !caseSensitive) {
            return cb.equal(cb.lower(path.as(String.class)), convertedValue.toString().toLowerCase());
        }
        return cb.equal(path, convertedValue);
    }

    private Predicate buildLikePredicate(Root<?> root, CriteriaBuilder cb, FieldMetadata fieldMetadata, String value, boolean caseSensitive) {
        Path<String> path = root.get(fieldMetadata.name());
        String pattern = "%" + value + "%";

        if (!caseSensitive) {
            return cb.like(cb.lower(path), pattern.toLowerCase());
        }
        return cb.like(path, pattern);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private Predicate buildGreaterThanPredicate(Root<?> root, CriteriaBuilder cb, FieldMetadata fieldMetadata, String value) {
        Comparable convertedValue = (Comparable) convert(value, fieldMetadata.type());
        return cb.greaterThan(root.get(fieldMetadata.name()), convertedValue);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private Predicate buildGreaterThanOrEqualPredicate(Root<?> root, CriteriaBuilder cb, FieldMetadata fieldMetadata, String value) {
        Comparable convertedValue = (Comparable) convert(value, fieldMetadata.type());
        return cb.greaterThanOrEqualTo(root.get(fieldMetadata.name()), convertedValue);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private Predicate buildLessThanPredicate(Root<?> root, CriteriaBuilder cb, FieldMetadata fieldMetadata, String value) {
        Comparable convertedValue = (Comparable) convert(value, fieldMetadata.type());
        return cb.lessThan(root.get(fieldMetadata.name()), convertedValue);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private Predicate buildLessThanOrEqualPredicate(Root<?> root, CriteriaBuilder cb, FieldMetadata fieldMetadata, String value) {
        Comparable convertedValue = (Comparable) convert(value, fieldMetadata.type());
        return cb.lessThanOrEqualTo(root.get(fieldMetadata.name()), convertedValue);
    }

    private Predicate buildInPredicate(Root<?> root, CriteriaBuilder cb, FieldMetadata fieldMetadata, String value) {
        String[] parts = value.split(",");
        CriteriaBuilder.In<Object> in = cb.in(root.get(fieldMetadata.name()));
        for (String part : parts) {
            in.value(convert(part, fieldMetadata.type()));
        }
        return in;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private Predicate buildBetweenPredicate(Root<?> root, CriteriaBuilder cb, FieldMetadata fieldMetadata, String min, String max) {
        Comparable convertedMin = (Comparable) convert(min, fieldMetadata.type());
        Comparable convertedMax = (Comparable) convert(max, fieldMetadata.type());
        return cb.between(root.get(fieldMetadata.name()), convertedMin, convertedMax);
    }

    private Object convert(String value, Class<?> targetType) {
        if (conversionService.canConvert(String.class, targetType)) {
            return conversionService.convert(value, targetType);
        }
        // Fallback or throw exception? For now, just return value
        return value;
    }
}
