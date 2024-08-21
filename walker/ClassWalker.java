package walker;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

class UUIDHaver {
    UUID myId;
}

class ThingHaver {
    Object thing;
}

class ThingReturner extends ThingHaver {

    public UUIDHaver getUUIDHaver() {
        if (thing instanceof UUIDHaver) {
            return (UUIDHaver) thing;
        }
        return null;
    }
}

class GenericThingReturner extends ThingHaver {

    public List<UUIDHaver> getUUIDHaver() {
        List<UUIDHaver> list = new ArrayList<>();
        if (thing instanceof UUIDHaver) {
            list.add((UUIDHaver) thing);
        }
        return list;
    }
}

public class ClassWalker {
    public static void main(String [] args) throws Exception {
        Class<?> clazz = HashMap.class;
        System.out.println(clazz + ":");
        System.out.println(canFindClassInClass(clazz, new HashSet<>(), UUID.class)); //false

        clazz = UUIDHaver.class;
        System.out.println(clazz + ":");
        System.out.println(canFindClassInClass(clazz, new HashSet<>(), UUID.class)); //true

        clazz = ThingHaver.class;
        System.out.println(clazz + ":");
        System.out.println(canFindClassInClass(clazz, new HashSet<>(), UUID.class)); //false. 
        // Even if thingHaver's object can be a UUIDHaver, the class definition doesn't suggest a UUID is supported.

        clazz = ThingReturner.class;
        System.out.println(clazz + ":");
        System.out.println(canFindClassInClass(clazz, new HashSet<>(), UUID.class)); //true

        clazz = GenericThingReturner.class;
        System.out.println(clazz + ":");       
        System.out.println(canFindClassInClass(clazz, new HashSet<>(), UUID.class)); //true
    }

    /**
     * Basically, just tell me if soughtClass CAN EVER exist in startingClass, recursively, anywhere in the class structure.
     * 
     * @param startingClass
     * @param visited
     * @param soughtClass
     * @return
     */
    public static boolean canFindClassInClass(Class<?> startingClass, Set<Class<?>> visited, Class<?> soughtClass) {
            
        if(visited.contains(startingClass)) {
            return false;
        }
        visited.add(startingClass);
        

        List<Field> allFields = getAllFields(startingClass, Object.class);
        for(Field f : allFields) {
            Class<?> type = f.getType();
            if(soughtClass.isAssignableFrom(type)) {
                return true;
            }
            boolean filter = canFindClassInClass(type, visited, soughtClass);
            if (filter) {
                return true;
            }

        }
        Set<Class<?>> allFunctionReturns = getAllFunctionReturns(startingClass, Object.class);
        for(Class<?> c : allFunctionReturns) {
            if(soughtClass.isAssignableFrom(c)) {
                return true;
            }
            boolean filter = canFindClassInClass(c, visited, soughtClass);
            if (filter) {
                return true;
            } 
        }

        return false;
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

    private static Set<Class<?>> getAllFunctionReturns(Class<?> start, Class<?> exclusiveParent) {
        
        List<Method> allMethods = new ArrayList<>(Arrays.asList(start.getDeclaredMethods()));
        Set<Class<?>> allFields = new HashSet<>();
        for (Method method : allMethods) {
            allFields.add(method.getReturnType());
            Type genericType = method.getGenericReturnType();
            if (genericType instanceof ParameterizedType) {
                ParameterizedType parameterizedType = (ParameterizedType) genericType;
                Type[] elementTypes = parameterizedType.getActualTypeArguments();
                for(Type elementType : elementTypes) {
                    if (elementType instanceof Class) {
                        Class<?> elementType2 = (Class<?>)elementType;
                        allFields.add(elementType2);
                    }
                }
            }
            
        }

        Class<?> parent = start.getSuperclass();

        if(parent != null &&
            (exclusiveParent == null || !(parent.equals(exclusiveParent)))
        ) {
            Set<Class<?>> inheritedFields = getAllFunctionReturns(parent, exclusiveParent);
            allFields.addAll(inheritedFields);
        }

        return allFields;
    }
}
