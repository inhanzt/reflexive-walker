package walker;

import java.util.Collections;
import java.util.HashSet;
import java.util.UUID;

public class ReflexiveExaminer {

    public enum Ternary {
        FOUND("FOUND"),
        MISSING_BUT_POSSIBLE("MISSING_BUT_POSSIBLE"),
        NOT_FOUND("NOT_FOUND");

        private final String name;

        private Ternary(String str) {
            name = str;
        }

        public String toString() {
            return name;
        }
    }

    public static void main(String[] args) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        
        UUIDHaver uuidHaver = new UUIDHaver();
        uuidHaver.myId = UUID.randomUUID();        

        ThingHaver thingHaver = new ThingHaver();
        thingHaver.thing = uuidHaver;

        ThingReturner thingReturner = new ThingReturner();
        thingReturner.thing = uuidHaver;

        GenericThingReturner genericThingReturner = new GenericThingReturner();
        genericThingReturner.thing = uuidHaver;

        // Class doesn't specify thing, but we have the thing anyway.
        Ternary missingValueOnThing = isMissingValueOnThing(thingHaver, UUIDHaver.class, "myId", uuidHaver.myId);
        System.out.println(missingValueOnThing); // FOUND

        // Class doesn't specify thing, but we have the thing anyway, but the value is wrong.
        missingValueOnThing = isMissingValueOnThing(thingHaver, UUIDHaver.class, "myId", UUID.randomUUID());
        System.out.println(missingValueOnThing); // NOT_FOUND

        // Class doesn't specify thing, and we don't have the thing.
        thingHaver.thing = new Object();
        missingValueOnThing = isMissingValueOnThing(thingHaver, UUIDHaver.class, "myId", uuidHaver.myId);
        System.out.println(missingValueOnThing); // NOT_FOUND        

        // Class does specify thing, and we have the thing.
        missingValueOnThing = isMissingValueOnThing(thingReturner, UUIDHaver.class, "myId", uuidHaver.myId);
        System.out.println(missingValueOnThing); // FOUND

        // Class does specify thing, and we have the thing, but the value is wrong.
        missingValueOnThing = isMissingValueOnThing(thingReturner, UUIDHaver.class, "myId",  UUID.randomUUID());
        System.out.println(missingValueOnThing); // MISSING_BUT_POSSIBLE

        // Class does specify thing, and we don't have the thing.
        thingReturner.thing = new Object();
        missingValueOnThing = isMissingValueOnThing(thingReturner, UUIDHaver.class, "myId", uuidHaver.myId);
        System.out.println(missingValueOnThing); // MISSING_BUT_POSSIBLE        

        // Class does specify thing through generics, and we have the thing.
        missingValueOnThing = isMissingValueOnThing(genericThingReturner, UUIDHaver.class, "myId", uuidHaver.myId);
        System.out.println(missingValueOnThing); // FOUND

        // Class does specify thing through generics, and we have the thing, but the value is wrong.
        missingValueOnThing = isMissingValueOnThing(genericThingReturner, UUIDHaver.class, "myId",  UUID.randomUUID());
        System.out.println(missingValueOnThing); // MISSING_BUT_POSSIBLE

        // Class does specify thing through generics, and we don't have the thing.
        genericThingReturner.thing = new Object();
        missingValueOnThing = isMissingValueOnThing(genericThingReturner, UUIDHaver.class, "myId", uuidHaver.myId);
        System.out.println(missingValueOnThing); // MISSING_BUT_POSSIBLE        

    }

    public static Ternary isMissingValueOnThing(Object examinedObject, Class<?> soughtThingClass, String soughtVariableName, Object soughtValue) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        boolean canFindClassInClass = ClassWalker.canFindClassInClass(examinedObject.getClass(), new HashSet<>(), soughtThingClass);
        boolean hasValueInObject = ObjectWalker.hasValueInObject(examinedObject, Collections.singleton(soughtValue), soughtThingClass, "myId");

        if(hasValueInObject) {
            return Ternary.FOUND;
        } else if(canFindClassInClass) {
            return Ternary.MISSING_BUT_POSSIBLE;
        } else {
            return Ternary.NOT_FOUND;
        }
    }

}
