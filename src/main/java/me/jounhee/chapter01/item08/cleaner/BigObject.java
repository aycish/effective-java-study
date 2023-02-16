package me.jounhee.chapter01.item08.cleaner;

import java.util.List;

public class BigObject {

    private List<Object> resource;

    public BigObject(List<Object> resource) {
        this.resource = resource;
    }

    // Runnable 구현체 안에는 BigObject에 대한 참조가 없어야한다. 그래야 확실하게 GC의 대상이 되기 때문.
    public static class ResourceCleaner implements Runnable {

        private List<Object> resourceToClean;

        public ResourceCleaner(List<Object> resourceToClean) {
            this.resourceToClean = resourceToClean;
        }

        @Override
        public void run() {
            resourceToClean = null;
            System.out.println("cleaned up.");
        }
    }
}
