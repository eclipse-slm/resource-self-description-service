package org.eclipse.slm.self_description_service.datasource.docker;

import com.github.dockerjava.api.model.DockerObject;
import org.apache.poi.hpsf.Decimal;
import org.eclipse.digitaltwin.aas4j.v3.model.*;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.*;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.*;
import java.util.function.Function;

public class DockerSubmodel extends DefaultSubmodel {
    public static final String SUBMODELID = "DockerInfo";
    public static final String SEMANTIC_ID_VALUE = "http://eclipse.dev/slm/aas/sm/DockerInfo";
    public static final Reference SEMANTIC_ID = new DefaultReference.Builder().keys(
            new DefaultKey.Builder()
                    .type(KeyTypes.CONCEPT_DESCRIPTION)
                    .value(SEMANTIC_ID_VALUE).build()).build();


    private final SimpleDateFormat dateTimeFormat = new SimpleDateFormat("MM/dd/yyyy KK:mm:ss a");
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");


    public DockerSubmodel(String resourceId) {
        super();
        this.id = SUBMODELID + "-" + resourceId;
        this.idShort = SUBMODELID;
        setSemanticId(SEMANTIC_ID);
    }


    public Optional<?> getContainers() {
        return getElementCollection("Containers");
    }

    public Optional<?> getImages() {
        return getElementCollection("Images");
    }

    public Optional<?> getNetworks() {
        return getElementCollection("Networks");
    }

    public Optional<?> getVolumes() {
        return getElementCollection("Volumes");
    }

    public Optional<?> getServices() {
        return getElementCollection("Services");
    }

    public Optional<?> getTasks() {
        return getElementCollection("Tasks");
    }

    public Optional<?> getSwarmNodes() {
        return getElementCollection("Swarm Nodes");
    }

    public Optional<?> getConfigs() {
        return getElementCollection("Configs");
    }

    public Optional<?> getSecrets() {
        return getElementCollection("Secrets");
    }


    public Optional<SubmodelElement> getSubmodelElementByShortId(String shortId) {
        return this.submodelElements.stream().filter(elem -> elem.getIdShort().equals(shortId)).findFirst();
    }

    private Optional<SubmodelElementList> getElementCollection(String shortId) {
        var collection = this.getSubmodelElementByShortId(shortId);
        if (collection.isPresent()) {
            var containers = collection.get();
            if (containers instanceof SubmodelElementList) {
                return Optional.of((SubmodelElementList) containers);
            }
        }
        return Optional.empty();
    }


    public <T> void addSubmodelEntry(String name, List<T> values) {
        var optionalList = createList(name, values);
        optionalList.ifPresent(submodelElementList -> this.submodelElements.add(submodelElementList));
    }


    public <T> Optional<SubmodelElementList> createList(String name, List<T> values) {

        if (name == null || values == null || values.isEmpty()) {
            return Optional.empty();
        }

        var listBuilder = new DefaultSubmodelElementList.Builder()
                .displayName(new DefaultLangStringNameType.Builder().text(name).build());
        listBuilder.idShort(name);

        for (var value : values) {
            if (value instanceof DockerObject dockerObject) {
                createCollectionValue(name, dockerObject).ifPresent(listBuilder::value);
            } else if (value instanceof Collection<?> listValue) {
                var listValues = listValue.stream().map(o -> (Object) o).toList();
                createList(listValues).ifPresent(listBuilder::value);
            } else if (value instanceof Map<?, ?> map) {
                createMap(name, this.mapMapValues(map)).ifPresent(listBuilder::value);
            } else {
                createProperty(name, value).ifPresent(listBuilder::value);
            }
        }

        return Optional.of(listBuilder.build());
    }

    private Optional<SubmodelElementCollection> createCollectionValue(String name, DockerObject dockerObject) {

        if (name == null || dockerObject == null) {
            return Optional.empty();
        }

        var collectionBuilder = new DefaultSubmodelElementCollection.Builder()
                .displayName(new DefaultLangStringNameType.Builder().text(name).build());
        collectionBuilder.idShort(name);

        var values = dockerObject.getRawValues();
        values.forEach((key, value) -> {

            if (value == null) {
                return;
            }

            if (value instanceof DockerObject v) {
                createCollectionValue(key, v).ifPresent(collectionBuilder::value);
            } else if (value instanceof Collection<?> v) {
                var listValues = v.stream().map(o -> (Object) o).toList();
                if (!listValues.isEmpty()) {
                    createList(key, listValues).ifPresent(collectionBuilder::value);
                }
            } else if (value instanceof Map<?, ?> map) {
                createMap(key, this.mapMapValues(map)).ifPresent(collectionBuilder::value);
            } else {
                createProperty(key, value).ifPresent(collectionBuilder::value);
            }

        });


        return Optional.of(collectionBuilder.build());
    }

    private <T, U> Map<String, Object> mapMapValues(Map<T, U> map) {
        var result = new HashMap<String, Object>();
        map.forEach((key, value) -> {
            if (key instanceof String keyName) {
                result.put(keyName, value);
            }
        });
        return result;
    }

    private <T> Optional<SubmodelElementList> createList(Collection<T> values) {
        if (values == null || values.isEmpty()) {
            return Optional.empty();
        }
        var listBuilder = new DefaultSubmodelElementList.Builder();


        for (var value : values) {
            var className = value.getClass().getName();

            if (value instanceof DockerObject dockerObject) {
                createCollectionValue(className, dockerObject).ifPresent(listBuilder::value);
            } else if (value instanceof Collection<?> listValue) {
                var listValues = listValue.stream().map(o -> (Object) o).toList();
                createList(listValues).ifPresent(listBuilder::value);
            } else if (value instanceof Map<?, ?> map) {
                var m = new HashMap<String, Object>();
                map.forEach((k, v) -> {
                    if (k instanceof String keyName) {
                        m.put(keyName, v);
                    }
                });
                createMap(className, m).ifPresent(listBuilder::value);
            } else {
                createProperty(value).ifPresent(listBuilder::value);
            }
        }

        return Optional.of(listBuilder.build());
    }

    private <T> Optional<SubmodelElementList> createMap(String name, Map<String, T> map) {
        if (map == null || map.isEmpty()) {
            return Optional.empty();
        }

        var mapBuilder = new DefaultSubmodelElementList.Builder()
                .displayName(new DefaultLangStringNameType.Builder().text(name).build())
                .idShort(name);

        map.forEach((mapKey, mapValue) -> {
            createMapElement(mapKey, mapValue).ifPresent(mapBuilder::value);
        });

        return Optional.of(mapBuilder.build());
    }

    private <T> Optional<? extends SubmodelElement> createMapElement(String name, T value) {
        if (name == null || value == null) {
            return Optional.empty();
        }

        if (value instanceof DockerObject dockerObject) {
            return createCollectionValue(name, dockerObject);
        } else if (value instanceof Collection<?> listValue) {
            var listValues = listValue.stream().map(o -> (Object) o).toList();
            return createList(name, listValues);
        } else if (value instanceof Map<?, ?> map) {
            var m = new HashMap<String, Object>();
            map.forEach((k, v) -> {
                if (k instanceof String keyName) {
                    m.put(keyName, v);
                }
            });
            return createMap(name, m);
        } else {
            return createProperty(name, value);
        }

    }

    private <T> Optional<Property> createProperty(T value) {

        if (value == null) {
            return Optional.empty();
        }

        var property = new DefaultProperty.Builder();
        setValueForProperty(value, property);

        return Optional.of(property.build());
    }

    private <T> Optional<Property> createProperty(String name, T value) {

        if (value == null) {
            return Optional.empty();
        }

        var property = new DefaultProperty.Builder()
                .displayName(new DefaultLangStringNameType.Builder().text(name).build())
                .idShort(name);
        setValueForProperty(value, property);

        return Optional.of(property.build());
    }

    private <T> void setValueForProperty(T value, DefaultProperty.Builder property) {
        if (value instanceof String v) {
            property.valueType(DataTypeDefXsd.STRING).value(v);
        } else if (value instanceof Integer v) {
            property.valueType(DataTypeDefXsd.INTEGER).value(v.toString());
        } else if (value instanceof Long v) {
            property.valueType(DataTypeDefXsd.LONG).value(v.toString());
        } else if (value instanceof Double v) {
            property.valueType(DataTypeDefXsd.DOUBLE).value(v.toString());
        } else if (value instanceof Float v) {
            property.valueType(DataTypeDefXsd.FLOAT).value(v.toString());
        } else if (value instanceof Decimal v) {
            property.valueType(DataTypeDefXsd.DECIMAL).value(v.toString());
        } else if (value instanceof Boolean v) {
            property.valueType(DataTypeDefXsd.BOOLEAN).value(v.toString());
        } else if (value instanceof Date v) {
            property.valueType(DataTypeDefXsd.DATE_TIME).value(this.dateTimeFormat.format(v));
        } else if (value instanceof Byte v) {
            property.valueType(DataTypeDefXsd.BYTE).value(v.toString());
        } else if (value instanceof Duration v) {
            property.valueType(DataTypeDefXsd.DURATION).value(v.toString());
        } else if (value instanceof Enum<?> v) {
            property.valueType(DataTypeDefXsd.STRING).value(v.name());
        }
    }


    private void addProperty(DefaultSubmodelElementCollection.Builder collectionBuilder, String value, String name) {
        if (value != null) {
            collectionBuilder.value(new DefaultProperty.Builder()
                    .valueType(DataTypeDefXsd.STRING)
                    .displayName(new DefaultLangStringNameType.Builder().text(name).build())
                    .value(value)
                    .build());
        }
    }


    private void addDateTimeProperty(DefaultSubmodelElementCollection.Builder collectionBuilder, Date dateTime, String name) {
        if (dateTime != null) {
            collectionBuilder.value(new DefaultProperty.Builder()
                    .valueType(DataTypeDefXsd.DATE_TIME)
                    .value(dateTimeFormat.format(dateTime))
                    .displayName(new DefaultLangStringNameType.Builder().text(name).build())
                    .build());
        }
    }

    private void addDateProperty(DefaultSubmodelElementCollection.Builder collectionBuilder, Date date, String name) {
        if (date != null) {
            collectionBuilder.value(new DefaultProperty.Builder()
                    .valueType(DataTypeDefXsd.DATE)
                    .value(dateFormat.format(date))
                    .displayName(new DefaultLangStringNameType.Builder().text(name).build())
                    .build());
        }
    }

    private void addProperty(DefaultSubmodelElementCollection.Builder collectionBuilder, Integer value, String name) {
        if (value != null) {
            collectionBuilder.value(new DefaultProperty.Builder()
                    .valueType(DataTypeDefXsd.INTEGER)
                    .value(value.toString())
                    .displayName(new DefaultLangStringNameType.Builder().text(name).build())
                    .build());
        }
    }

    private void addProperty(DefaultSubmodelElementCollection.Builder collectionBuilder, int value, String name) {
        collectionBuilder.value(new DefaultProperty.Builder()
                .valueType(DataTypeDefXsd.INTEGER)
                .value(String.valueOf(value))
                .displayName(new DefaultLangStringNameType.Builder().text(name).build())
                .build());
    }

    private void addProperty(DefaultSubmodelElementCollection.Builder collectionBuilder, Long value, String name) {
        if (value != null) {
            collectionBuilder.value(new DefaultProperty.Builder()
                    .valueType(DataTypeDefXsd.LONG)
                    .value(String.valueOf(value))
                    .displayName(new DefaultLangStringNameType.Builder().text(name).build())
                    .build());
        }
    }

    private void addDateTimeProperty(DefaultSubmodelElementCollection.Builder collectionBuilder, Long dateTime, String name) {
        if (dateTime != null) {
            collectionBuilder.value(new DefaultProperty.Builder()
                    .valueType(DataTypeDefXsd.DATE_TIME)
                    .value(dateTimeFormat.format(dateTime))
                    .displayName(new DefaultLangStringNameType.Builder().text(name).build())
                    .build());
        }
    }

    private void addProperty(DefaultSubmodelElementCollection.Builder collectionBuilder, Boolean value, String name) {
        if (value != null) {
            collectionBuilder.value(
                    new DefaultProperty.Builder()
                            .valueType(DataTypeDefXsd.BOOLEAN)
                            .value(value.toString())
                            .displayName(new DefaultLangStringNameType.Builder().text(name).build())
                            .build()
            );
        }
    }

    private void addProperty(DefaultSubmodelElementCollection.Builder collectionBuilder, Float value, String name) {
        if (value != null) {
            collectionBuilder.value(
                    new DefaultProperty.Builder()
                            .valueType(DataTypeDefXsd.FLOAT)
                            .value(value.toString())
                            .displayName(new DefaultLangStringNameType.Builder().text(name).build())
                            .build()
            );
        }
    }


    private void addProperty(DefaultSubmodelElementList.Builder builder, String value, String name) {
        if (value != null) {
            builder.value(new DefaultProperty.Builder()
                    .valueType(DataTypeDefXsd.STRING)
                    .displayName(new DefaultLangStringNameType.Builder().text(name).build())
                    .value(value)
                    .build());
        }
    }

    private void addDateTimeProperty(DefaultSubmodelElementList.Builder builder, Date dateTime, String name) {
        if (dateTime != null) {
            builder.value(new DefaultProperty.Builder()
                    .valueType(DataTypeDefXsd.DATE_TIME)
                    .value(dateTimeFormat.format(dateTime))
                    .displayName(new DefaultLangStringNameType.Builder().text(name).build())
                    .build());
        }
    }

    private void addDateProperty(DefaultSubmodelElementList.Builder builder, Date date, String name) {
        if (date != null) {
            builder.value(new DefaultProperty.Builder()
                    .valueType(DataTypeDefXsd.DATE)
                    .value(dateFormat.format(date))
                    .displayName(new DefaultLangStringNameType.Builder().text(name).build())
                    .build());
        }
    }

    private void addProperty(DefaultSubmodelElementList.Builder builder, Integer value, String name) {
        if (value != null) {
            builder.value(new DefaultProperty.Builder()
                    .valueType(DataTypeDefXsd.INTEGER)
                    .value(value.toString())
                    .displayName(new DefaultLangStringNameType.Builder().text(name).build())
                    .build());
        }
    }

    private void addProperty(DefaultSubmodelElementList.Builder builder, int value, String name) {
        builder.value(new DefaultProperty.Builder()
                .valueType(DataTypeDefXsd.INTEGER)
                .value(String.valueOf(value))
                .displayName(new DefaultLangStringNameType.Builder().text(name).build())
                .build());
    }

    private void addDateTimeProperty(DefaultSubmodelElementList.Builder builder, Long dateTime, String name) {
        if (dateTime != null) {
            builder.value(new DefaultProperty.Builder()
                    .valueType(DataTypeDefXsd.DATE_TIME)
                    .value(dateTimeFormat.format(dateTime))
                    .displayName(new DefaultLangStringNameType.Builder().text(name).build())
                    .build());
        }
    }

    private void addProperty(DefaultSubmodelElementList.Builder builder, Boolean value, String name) {
        if (value != null) {
            builder.value(
                    new DefaultProperty.Builder()
                            .valueType(DataTypeDefXsd.BOOLEAN)
                            .value(value.toString())
                            .displayName(new DefaultLangStringNameType.Builder().text(name).build())
                            .build()
            );
        }
    }


    private void addListOfStringProperties(DefaultSubmodelElementCollection.Builder builder, List<String> values, String listName, String name) {
        if (values != null && !values.isEmpty()) {
            var list = new DefaultSubmodelElementList.Builder()
                    .displayName(new DefaultLangStringNameType.Builder().text(listName).build());

            for (var value : values) {
                addProperty(list, value, name);
            }
            builder.value(list.build());
        }
    }

    private void addListOfStringProperties(DefaultSubmodelElementCollection.Builder builder, String[] values, String listName, String name) {
        if (values != null && values.length > 0) {
            var list = new DefaultSubmodelElementList.Builder()
                    .displayName(new DefaultLangStringNameType.Builder().text(listName).build());

            for (var value : values) {
                addProperty(list, value, name);
            }
            builder.value(list.build());
        }
    }

    private void addListOfIntegerProperties(DefaultSubmodelElementCollection.Builder builder, List<Integer> values, String listName, String name) {
        if (values != null && !values.isEmpty()) {
            var list = new DefaultSubmodelElementList.Builder()
                    .displayName(new DefaultLangStringNameType.Builder().text(listName).build());

            for (var value : values) {

                addProperty(list, value, name);
            }
            builder.value(list.build());
        }
    }


    private void addLabels(DefaultSubmodelElementCollection.Builder containerCollectionBuilder, Map<String, String> labels) {
        if (labels != null && !labels.isEmpty()) {
            var labelsListBuilder = new DefaultSubmodelElementList.Builder()
                    .displayName(new DefaultLangStringNameType.Builder().text("Labels").build());

            labels.forEach((key, value) -> {
                var pair = new DefaultSubmodelElementCollection.Builder();
                addProperty(pair, key, "Label");
                addProperty(pair, value, "Value");
                labelsListBuilder.value(pair.build());
            });
            containerCollectionBuilder.value(labelsListBuilder.build());
        }
    }

    private void addOptions(DefaultSubmodelElementCollection.Builder builder, Map<String, String> options) {
        if (options != null && !options.isEmpty()) {
            var networkOptionsBuilder = new DefaultSubmodelElementList.Builder()
                    .displayName(new DefaultLangStringNameType.Builder().text("Options").build());

            options.forEach((key, value) -> {
                var pair = new DefaultSubmodelElementCollection.Builder();
                addProperty(pair, key, "Key");
                addProperty(pair, value, "Value");

                networkOptionsBuilder.value(pair.build());
            });

            builder.value(networkOptionsBuilder.build());
        }
    }


    public <T extends DockerObject> void addSubmodelEntry(String name, List<T> values, Function<T, String> keyExtractor) {
        var listBuilder = new DefaultSubmodelElementList.Builder()
                .displayName(new DefaultLangStringNameType.Builder().text(name).build());
        listBuilder.idShort(name);

        for (T value : values) {
            var valueName = keyExtractor.apply(value);
            var collection = createCollectionValue(valueName, value);
            collection.ifPresent(listBuilder::value);
        }
        this.submodelElements.add(listBuilder.build());
    }
}
