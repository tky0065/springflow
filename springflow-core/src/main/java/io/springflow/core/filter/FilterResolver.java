package io.springflow.core.filter;

import io.springflow.annotations.FilterType;
import io.springflow.annotations.Filterable;
import io.springflow.core.metadata.EntityMetadata;
import io.springflow.core.metadata.FieldMetadata;
import io.springflow.core.metadata.RelationMetadata;
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
     * @param params   the query parameters map
     * @param metadata the entity metadata
     * @param <T>      the entity type
     * @return a Specification that can be used with JpaSpecificationExecutor
     */
    public <T> Specification<T> buildSpecification(Map<String, String[]> params, EntityMetadata metadata) {
        return buildSpecification(params, metadata, null);
    }

    /**
     * Builds a JPA Specification from query parameters and entity metadata, with support for fetch joins.
     *
     * @param params      the query parameters map
     * @param metadata    the entity metadata
     * @param fetchFields list of relation fields to fetch eagerly
     * @param <T>         the entity type
     * @return a Specification that can be used with JpaSpecificationExecutor
     */
    public <T> Specification<T> buildSpecification(Map<String, String[]> params, EntityMetadata metadata, List<String> fetchFields) {
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
                    // Default: fetch all non-hidden single-value relations to avoid N+1
                    // Collection relations (OneToMany, ManyToMany) are NOT fetched by default
                    // to avoid MultipleBagFetchException.
                    for (FieldMetadata fieldMetadata : metadata.fields()) {
                        if (fieldMetadata.isRelation() && !fieldMetadata.hidden()) {
                            RelationMetadata.RelationType type = fieldMetadata.relation().type();
                            if (type == RelationMetadata.RelationType.MANY_TO_ONE || 
                                type == RelationMetadata.RelationType.ONE_TO_ONE) {
                                root.fetch(fieldMetadata.name(), jakarta.persistence.criteria.JoinType.LEFT);
                            }
                        }
                    }
                    query.distinct(true);
                }
            }

            for (FieldMetadata fieldMetadata : metadata.fields()) {
                if (fieldMetadata.filterConfig() != null) {
                    log.trace("Field {} has filter config, processing predicates", fieldMetadata.name());
                    predicates.addAll(buildPredicatesForField(root, cb, fieldMetadata, params));
                } else {
                    log.trace("Field {} has NO filter config", fieldMetadata.name());
                }
            }

            return predicates.isEmpty() ? cb.conjunction() : cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private List<Predicate> buildPredicatesForField(Root<?> root, CriteriaBuilder cb, FieldMetadata fieldMetadata, Map<String, String[]> params) {
        List<Predicate> predicates = new ArrayList<>();
        Filterable config = fieldMetadata.filterConfig();
        String baseParamName = StringUtils.hasText(config.paramName()) ? config.paramName() : fieldMetadata.name();
        List<FilterType> supportedTypes = Arrays.asList(config.types());

        // Process all parameters to find matches for this field (e.g., field, field[eq], field_like)
        for (Map.Entry<String, String[]> entry : params.entrySet()) {
            String paramKey = entry.getKey();
            String[] paramValues = entry.getValue();
            if (paramValues == null || paramValues.length == 0) continue;
            String firstValue = paramValues[0];

            log.trace("Checking param {} against field {} (baseParamName: {})", paramKey, fieldMetadata.name(), baseParamName);

            // 1. Exact match (EQUALS)
            if (paramKey.equals(baseParamName)) {
                if (supportedTypes.contains(FilterType.EQUALS)) {
                    log.debug("Applying EQUALS filter for field {}: {}", fieldMetadata.name(), firstValue);
                    predicates.add(buildEqualsPredicate(root, cb, fieldMetadata, firstValue, config.caseSensitive()));
                }
                continue;
            }

            // 2. Bracketed format: field[op]=value
            if (paramKey.startsWith(baseParamName + "[") && paramKey.endsWith("]")) {
                String op = paramKey.substring(baseParamName.length() + 1, paramKey.length() - 1);
                FilterType type = resolveOperator(op);
                log.trace("Found bracketed op {} for field {}, resolved to FilterType {}", op, fieldMetadata.name(), type);
                if (type != null && (supportedTypes.contains(type) || 
                    (supportedTypes.contains(FilterType.RANGE) && (type == FilterType.GREATER_THAN || type == FilterType.GREATER_THAN_OR_EQUAL || 
                                                                  type == FilterType.LESS_THAN || type == FilterType.LESS_THAN_OR_EQUAL)))) {
                    log.debug("Applying {} filter for field {}: {}", type, fieldMetadata.name(), firstValue);
                    predicates.add(buildPredicateByType(root, cb, fieldMetadata, type, firstValue, config.caseSensitive()));
                }
                continue;
            }

            // 3. Underscore format: field_op=value (legacy)
            if (paramKey.startsWith(baseParamName + "_")) {
                String op = paramKey.substring(baseParamName.length() + 1);
                FilterType type = resolveOperator(op);
                if (type != null && (supportedTypes.contains(type) || 
                    (supportedTypes.contains(FilterType.RANGE) && (type == FilterType.GREATER_THAN || type == FilterType.GREATER_THAN_OR_EQUAL || 
                                                                  type == FilterType.LESS_THAN || type == FilterType.LESS_THAN_OR_EQUAL)))) {
                    log.debug("Applying {} filter (underscore) for field {}: {}", type, fieldMetadata.name(), firstValue);
                    predicates.add(buildPredicateByType(root, cb, fieldMetadata, type, firstValue, config.caseSensitive()));
                }
            }
        }

        return predicates;
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(FilterResolver.class);

    private FilterType resolveOperator(String op) {
        return switch (op.toLowerCase()) {
            case "eq", "equals" -> FilterType.EQUALS;
            case "like", "contains" -> FilterType.LIKE;
            case "gt" -> FilterType.GREATER_THAN;
            case "gte", "ge" -> FilterType.GREATER_THAN_OR_EQUAL;
            case "lt" -> FilterType.LESS_THAN;
            case "lte", "le" -> FilterType.LESS_THAN_OR_EQUAL;
            case "in" -> FilterType.IN;
            case "nin", "not_in" -> FilterType.NOT_IN;
            case "null", "isnull" -> FilterType.IS_NULL;
            case "between" -> FilterType.BETWEEN;
            default -> null;
        };
    }

    private Predicate buildPredicateByType(Root<?> root, CriteriaBuilder cb, FieldMetadata fieldMetadata, 
                                          FilterType type, String value, boolean caseSensitive) {
        return switch (type) {
            case EQUALS -> buildEqualsPredicate(root, cb, fieldMetadata, value, caseSensitive);
            case LIKE -> buildLikePredicate(root, cb, fieldMetadata, value, caseSensitive);
            case GREATER_THAN -> buildGreaterThanPredicate(root, cb, fieldMetadata, value);
            case GREATER_THAN_OR_EQUAL -> buildGreaterThanOrEqualPredicate(root, cb, fieldMetadata, value);
            case LESS_THAN -> buildLessThanPredicate(root, cb, fieldMetadata, value);
            case LESS_THAN_OR_EQUAL -> buildLessThanOrEqualPredicate(root, cb, fieldMetadata, value);
            case IN -> buildInPredicate(root, cb, fieldMetadata, value);
            case NOT_IN -> cb.not(buildInPredicate(root, cb, fieldMetadata, value));
            case IS_NULL -> {
                boolean isNull = Boolean.parseBoolean(value);
                yield isNull ? cb.isNull(root.get(fieldMetadata.name())) : cb.isNotNull(root.get(fieldMetadata.name()));
            }
            case BETWEEN -> {
                String[] parts = value.split(",");
                yield (parts.length == 2) ? buildBetweenPredicate(root, cb, fieldMetadata, parts[0], parts[1]) : cb.conjunction();
            }
            default -> cb.conjunction();
        };
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
