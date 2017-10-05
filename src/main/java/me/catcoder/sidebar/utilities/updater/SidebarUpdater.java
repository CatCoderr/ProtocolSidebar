package me.catcoder.sidebar.utilities.updater;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import lombok.Getter;
import lombok.NonNull;
import me.catcoder.sidebar.Sidebar;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Автоматический апдейтер для скорборда.
 *
 * @author CatCoder
 */
public class SidebarUpdater {


    private final Multimap<Long, Task> tasks =
            Multimaps.synchronizedListMultimap(
                    Multimaps.newListMultimap(
                            Maps.newHashMap(),
                            ArrayList::new));
    @Getter
    private final Sidebar sidebar;
    @Getter
    private final ExecutorService executorService;

    @Getter
    private volatile boolean started;

    /**
     * Конструктор апдейтера
     *
     * @param sidebar  - скорборд
     * @param executor - выполнитель
     */
    SidebarUpdater(@NonNull Sidebar sidebar, @NonNull ExecutorService executor) {
        this.sidebar = sidebar;
        this.executorService = executor;
    }

    public static SidebarUpdater newUpdater(Sidebar sidebar, ExecutorService executorService) {
        return new SidebarUpdater(sidebar, executorService);
    }

    /**
     * Получение списка задач.
     *
     * @return список задач
     */
    public Multimap<Long, Task> getTasks() {
        return Multimaps.unmodifiableMultimap(tasks);
    }

    /**
     * Очистка всех задач.
     */
    public void clearTasks() {
        this.tasks.clear();
    }

    /**
     * Остановка апдейтера.
     */
    public void stop() {
        Preconditions.checkState(isStarted(), "Updating is not started.");
        executorService.shutdownNow();
    }

    /**
     * Добавление новой задачи
     *
     * @param task  - реализация задачи
     * @param delay - период ее вызова
     * @return инстанс этого класса
     */
    public SidebarUpdater newTask(@NonNull Task task, long delay) {
        if (delay < 0) throw new IllegalArgumentException("Delay value must be > 0");
        tasks.put(delay, task);
        return this;
    }

    /**
     * Стартовать обновление
     */
    public void start() {
        Preconditions.checkState(!isStarted(), "Updating already started.");
        Preconditions.checkState(!executorService.isTerminated(), "Executor service is terminated.");

        startTaskExecution(); //Lets rock!

        started = true;
    }

    /**
     * Внутренний метод для выполнения задач
     */
    private void startTaskExecution() {
        AtomicLong time = new AtomicLong();

        Runnable updater = () -> {
            while (true) {
                try {
                    Thread.sleep(50L);
                } catch (InterruptedException ignored) {
                }
                tasks.asMap().entrySet()
                        .stream()
                        .filter(entry -> time.get() % entry.getKey() == 0)
                        .forEach(entry -> entry.getValue().forEach(this::execute));

                if (time.incrementAndGet() == Long.MAX_VALUE) { //Хм, кек
                    time.set(0); //Фиксим
                }
            }
        };

        executorService.execute(updater);
    }

    /**
     * Выполнение задачи и обработка исключений при ее выполнении.
     *
     * @param task - задача
     */
    public void execute(@NonNull Task task) {
        try {
            task.update(sidebar);
        } catch (Exception ex) {
            throw new UpdaterException("An exception occurred while executing task.", ex);
        }
    }

}
