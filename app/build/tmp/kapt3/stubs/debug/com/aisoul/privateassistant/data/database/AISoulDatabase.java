package com.aisoul.privateassistant.data.database;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000 \n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\b\'\u0018\u0000 \t2\u00020\u0001:\u0001\tB\u0005\u00a2\u0006\u0002\u0010\u0002J\b\u0010\u0003\u001a\u00020\u0004H&J\b\u0010\u0005\u001a\u00020\u0006H&J\b\u0010\u0007\u001a\u00020\bH&\u00a8\u0006\n"}, d2 = {"Lcom/aisoul/privateassistant/data/database/AISoulDatabase;", "Landroidx/room/RoomDatabase;", "()V", "aiModelDao", "Lcom/aisoul/privateassistant/data/dao/AIModelDao;", "conversationDao", "Lcom/aisoul/privateassistant/data/dao/ConversationDao;", "messageDao", "Lcom/aisoul/privateassistant/data/dao/MessageDao;", "Companion", "app_debug"})
@androidx.room.Database(entities = {com.aisoul.privateassistant.data.entities.Conversation.class, com.aisoul.privateassistant.data.entities.Message.class, com.aisoul.privateassistant.data.entities.AIModel.class}, version = 1, exportSchema = false)
public abstract class AISoulDatabase extends androidx.room.RoomDatabase {
    @kotlin.jvm.Volatile()
    @org.jetbrains.annotations.Nullable()
    private static volatile com.aisoul.privateassistant.data.database.AISoulDatabase INSTANCE;
    @org.jetbrains.annotations.NotNull()
    public static final com.aisoul.privateassistant.data.database.AISoulDatabase.Companion Companion = null;
    
    public AISoulDatabase() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public abstract com.aisoul.privateassistant.data.dao.ConversationDao conversationDao();
    
    @org.jetbrains.annotations.NotNull()
    public abstract com.aisoul.privateassistant.data.dao.MessageDao messageDao();
    
    @org.jetbrains.annotations.NotNull()
    public abstract com.aisoul.privateassistant.data.dao.AIModelDao aiModelDao();
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000&\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0019\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u0006\u0010\u0005\u001a\u00020\u0006J\u0016\u0010\u0007\u001a\u00020\u00042\u0006\u0010\b\u001a\u00020\t2\u0006\u0010\n\u001a\u00020\u000bR\u0010\u0010\u0003\u001a\u0004\u0018\u00010\u0004X\u0082\u000e\u00a2\u0006\u0002\n\u0000\u00a8\u0006\f"}, d2 = {"Lcom/aisoul/privateassistant/data/database/AISoulDatabase$Companion;", "", "()V", "INSTANCE", "Lcom/aisoul/privateassistant/data/database/AISoulDatabase;", "closeDatabase", "", "getDatabase", "context", "Landroid/content/Context;", "passphrase", "", "app_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.aisoul.privateassistant.data.database.AISoulDatabase getDatabase(@org.jetbrains.annotations.NotNull()
        android.content.Context context, @org.jetbrains.annotations.NotNull()
        char[] passphrase) {
            return null;
        }
        
        public final void closeDatabase() {
        }
    }
}