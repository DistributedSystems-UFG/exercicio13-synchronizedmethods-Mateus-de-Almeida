public class CounterTest {

    // ── Versão SEM sincronização ─────────────────────────────────────────────
    static class UnsafeCounter {
        private int c = 0;

        public void increment() { c++; }
        public void decrement() { c--; }
        public int value()      { return c; }
    }

    // ── Versão COM sincronização ─────────────────────────────────────────────
    static class SynchronizedCounter {
        private int c = 0;

        public synchronized void increment() { c++; }
        public synchronized void decrement() { c--; }
        public synchronized int value()      { return c; }
    }

    // ── Tarefa usada pelas threads ───────────────────────────────────────────
    // Cada thread faz OPERATIONS incrementos seguidos de OPERATIONS decrementos.
    // Resultado esperado ao final: contador == 0.
    static final int OPERATIONS = 100_000;

    static class UnsafeWorker implements Runnable {
        private final UnsafeCounter counter;
        UnsafeWorker(UnsafeCounter c) { this.counter = c; }

        public void run() {
            for (int i = 0; i < OPERATIONS; i++) counter.increment();
            for (int i = 0; i < OPERATIONS; i++) counter.decrement();
        }
    }

    static class SafeWorker implements Runnable {
        private final SynchronizedCounter counter;
        SafeWorker(SynchronizedCounter c) { this.counter = c; }

        public void run() {
            for (int i = 0; i < OPERATIONS; i++) counter.increment();
            for (int i = 0; i < OPERATIONS; i++) counter.decrement();
        }
    }

    // ── main ─────────────────────────────────────────────────────────────────
    public static void main(String[] args) throws InterruptedException {

        System.out.println("Cada thread executa " + OPERATIONS +
                           " incrementos + " + OPERATIONS + " decrementos.");
        System.out.println("Valor esperado ao final: 0\n");

        // --- Teste SEM sincronização ---
        UnsafeCounter unsafe = new UnsafeCounter();

        Thread u1 = new Thread(new UnsafeWorker(unsafe), "UnsafeThread-1");
        Thread u2 = new Thread(new UnsafeWorker(unsafe), "UnsafeThread-2");

        u1.start(); u2.start();
        u1.join();  u2.join();

        System.out.println("[SEM sincronização] Valor final: " + unsafe.value() +
                           (unsafe.value() == 0 ? "  ✓ correto" : "  ✗ inconsistente (race condition!)"));

        // --- Teste COM sincronização ---
        SynchronizedCounter safe = new SynchronizedCounter();

        Thread s1 = new Thread(new SafeWorker(safe), "SafeThread-1");
        Thread s2 = new Thread(new SafeWorker(safe), "SafeThread-2");

        s1.start(); s2.start();
        s1.join();  s2.join();

        System.out.println("[COM sincronização]  Valor final: " + safe.value() +
                           (safe.value() == 0 ? "  ✓ correto" : "  ✗ inconsistente"));
    }
}