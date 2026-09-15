class Parcel {

    private final int parcelId;
    private final String destination;

    public Parcel(int parcelId, String destination) {
        this.parcelId = parcelId;
        this.destination = destination;
    }

    @Override
    public String toString() {
        return "Parcel-" + parcelId + " [" + destination + "]";
    }
}

class ParcelConveyor {

    private final Parcel[] slots;
    private int insertPosition = 0;
    private int removePosition = 0;
    private int parcelCount = 0;

    public ParcelConveyor(int capacity) {
        slots = new Parcel[capacity];
    }

    public synchronized void load(Parcel parcel)
            throws InterruptedException {

        while (parcelCount == slots.length) {

            System.out.println(
                ">>> Conveyor FULL - Producer waiting..."
            );

            wait();
        }

        slots[insertPosition] = parcel;
        insertPosition = (insertPosition + 1) % slots.length;
        parcelCount++;

        System.out.println(
            "[PRODUCER] Loaded " + parcel +
            " | Conveyor = " +
            parcelCount + "/" + slots.length
        );

        notifyAll();
    }

    public synchronized Parcel unload()
            throws InterruptedException {

        while (parcelCount == 0) {

            System.out.println(
                "<<< Conveyor EMPTY - Consumer waiting..."
            );

            wait();
        }

        Parcel parcel = slots[removePosition];
        slots[removePosition] = null;
        removePosition = (removePosition + 1) % slots.length;
        parcelCount--;

        System.out.println(
            "[CONSUMER] Scanned " + parcel +
            " | Conveyor = " +
            parcelCount + "/" + slots.length
        );

        notifyAll();

        return parcel;
    }
}

class ParcelProducer extends Thread {

    private final ParcelConveyor conveyor;

    private final String[] cities = {
        "Bengaluru",
        "Mangaluru",
        "Mysuru",
        "Udupi",
        "Hubballi"
    };

    public ParcelProducer(ParcelConveyor conveyor) {
        this.conveyor = conveyor;
    }

    @Override
    public void run() {

        try {

            for (int i = 1; i <= 15; i++) {

                String destination =
                    cities[(i - 1) % cities.length];

                Parcel parcel =
                    new Parcel(i, destination);

                conveyor.load(parcel);

                Thread.sleep(150);
            }

            System.out.println(
                "\n[PRODUCER] All parcels generated."
            );

        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();
        }
    }
}

class ParcelConsumer extends Thread {

    private final ParcelConveyor conveyor;

    public ParcelConsumer(ParcelConveyor conveyor) {
        this.conveyor = conveyor;
    }

    @Override
    public void run() {

        try {

            for (int i = 1; i <= 15; i++) {

                Parcel parcel = conveyor.unload();

                Thread.sleep(350);

                System.out.println(
                    "             Processing completed -> "
                    + parcel
                );
            }

            System.out.println(
                "\n[CONSUMER] All parcels processed."
            );

        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();
        }
    }
}

public class SmartConveyor {

    public static void main(String[] args) {

        System.out.println(
            "=========================================="
        );

        System.out.println(
            "       SMART PARCEL CONVEYOR SYSTEM"
        );

        System.out.println(
            "==========================================\n"
        );

        ParcelConveyor conveyor =
            new ParcelConveyor(4);

        ParcelProducer producer =
            new ParcelProducer(conveyor);

        ParcelConsumer consumer =
            new ParcelConsumer(conveyor);

        producer.start();
        consumer.start();

        try {

            producer.join();
            consumer.join();

        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();
        }

        System.out.println(
            "\n=========================================="
        );

        System.out.println(
            "     PRODUCER-CONSUMER DEMO FINISHED"
        );

        System.out.println(
            "=========================================="
        );
    }
}