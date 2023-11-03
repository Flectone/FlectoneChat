package net.flectone.chat.database.sqlite;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class SQLiteExecutor {
    private final Database database;
    private final Statement statement;
    private final Thread thread;
    private final BlockingQueue<Runnable> taskQueue;

    public SQLiteExecutor(Database database) throws SQLException {
        this.database = database;
        this.statement = database.getConnection().createStatement();
        this.taskQueue = new LinkedBlockingQueue<>();
        this.thread = new Thread(this::run);
        this.thread.setName("SQLite Thread");
        this.thread.start();
    }

    public void executeStatement(StatementConsumer action) {
        try {
            taskQueue.put(() -> {
                try {
                    action.accept(database.getConnection());
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            });
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void executeRunnable(Runnable runnable) {
        try {
            taskQueue.put(runnable);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void close() {
        try {
            thread.interrupt();
            thread.join();
            statement.close();
            database.getConnection().close();
        } catch (InterruptedException | SQLException e) {
            e.printStackTrace();
        }
    }

    private void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                Runnable task = taskQueue.take();
                task.run();
            }
        } catch (InterruptedException e) {
            // Thread interrupted, exit the loop and close resources
        }
    }
}

