package uci.example;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

class UUIDHaver {
    UUID myId;
}

class ThingHaver {
    Object thing;
}

public class ObjectWalker {
    public static void main(String [] args) throws Exception {

        List<Object> myList = new ArrayList<>();
        Set<Object> mySet = new HashSet<>();
        Map<Object, Object> myMap = new HashMap<>();
        UUIDHaver uuidHaver = new UUIDHaver();
        uuidHaver.myId = UUID.randomUUID();

        myMap.put("string", mySet);
        mySet.add(myList);

        Set<UUID> seeking = new HashSet<>();

        // Not looking for anything, don't have anything.
        boolean hasValueInObject = hasValueInObject(myMap, seeking, UUIDHaver.class, "myId");
        System.out.println(hasValueInObject); // false

        // Do have something, not looking for anything.
        myList.add(uuidHaver);
        hasValueInObject = hasValueInObject(myMap, seeking, UUIDHaver.class, "myId");
        System.out.println(hasValueInObject); // false

        // Do have something, looking for wrong thing.
        seeking.add(UUID.randomUUID());
        hasValueInObject = hasValueInObject(myMap, seeking, UUIDHaver.class, "myId");
        System.out.println(hasValueInObject); // false

        // Looking for thing, have that thing.
        seeking.clear();
        seeking.add(uuidHaver.myId);
        hasValueInObject = hasValueInObject(myMap, seeking, UUIDHaver.class, "myId");
        System.out.println(hasValueInObject); // true

        // Looking for something, but don't have anything.
        myList.clear();
        hasValueInObject = hasValueInObject(myMap, seeking, UUIDHaver.class, "myId");
        System.out.println(hasValueInObject); // false

        // Looking for thing, have that thing. Outside of a collection.
        ThingHaver thingHaver = new ThingHaver();
        thingHaver.thing = uuidHaver;
        hasValueInObject = hasValueInObject(thingHaver, seeking, UUIDHaver.class, "myId");
        System.out.println(hasValueInObject); // true
    }

    public static boolean hasValueInObject(Object startingObject, Set<?> soughtValues, Class<?> soughtClass, String soughtFieldOfSoughtClass) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        
        Set<Object> foundValues = new HashSet<>();
        
        findValuesInObject(startingObject, new HashSet<>(), foundValues, soughtClass, soughtFieldOfSoughtClass);
        
        for(Object o : foundValues) {
            if(soughtValues.contains(o)) {
                return true;
            }
        }

        return false;
    }

    private static void findValuesInObject(Object o, Set<Object> visited, Set<Object> foundValues, Class<?> soughtClass, String soughtField) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        
        if(o == null) {
            return;
        }

        // If we found our thing; pull the data.
        if(soughtClass.isAssignableFrom(o.getClass())) {
            List<Field> allFields = getAllFields(soughtClass, Object.class);
            for(Field f : allFields) {
                if (soughtField.equals(f.getName())) {
                    f.setAccessible(true);
                    Object object = f.get(o);
                    foundValues.add(object);
                    return;
                }
            }
           throw new NoSuchFieldException(soughtField);
        }

        // If we found an array, we need to inspect each element.
        if(o.getClass().isArray()) {
            int length = Array.getLength(o);
            for(int i = 0; i < length; i++) {
                Object object = Array.get(o, i);
                if(visited.contains(object)) {
                    continue;
                }
                visited.add(o);
                findValuesInObject(object, visited, foundValues, soughtClass, soughtField);
            }
            return;
        }

        //Otherwise we have an arbitrary data structure, and we have to iterate through it.
        List<Field> fields = getAllFields(o.getClass(), Object.class);
        for(Field field : fields) {
            field.setAccessible(true);
            Object element = field.get(o);
            if(visited.contains(element)) {
                continue;
            }
            visited.add(element);
            findValuesInObject(element, visited, foundValues, soughtClass, soughtField);
        }
    }

    private static List<Field> getAllFields(Class<?> start, Class<?> exclusiveParent) {

        List<Field> allFields = new ArrayList<>(Arrays.asList(start.getDeclaredFields()));

        Class<?> parent = start.getSuperclass();

        if(parent != null &&
            (exclusiveParent == null || !(parent.equals(exclusiveParent)))
        ) {
            List<Field> inheritedFields = getAllFields(parent, exclusiveParent);
            allFields.addAll(inheritedFields);
        }

        return allFields;
    }

}
