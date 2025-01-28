        package com.menasy.heskit;

        import android.content.Context;

        public class Singleton {

            private static Singleton instance;
            private DBHelper dataBase;

            private Singleton() { }

            public static synchronized Singleton getInstance() {
                if (instance == null) {
                    instance = new Singleton();
                }
                return instance;
            }

            public void initDatabase(Context context) {
                if (dataBase == null) {
                    dataBase = new DBHelper(context.getApplicationContext());
                    }
            }

            public DBHelper getDataBase() {
                if (dataBase == null) {
                    throw new IllegalStateException("Database is not initialized. Call initDatabase() first.");
                }
                return dataBase;
            }
        }
