package com.aisoul.privateassistant.data.repository;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000Z\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\b\u0003\n\u0002\u0010\u0019\n\u0002\b\u0002\n\u0002\u0010\t\n\u0002\b\f\u0018\u0000 *2\u00020\u0001:\u0001*B\u000f\b\u0002\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u0006\u0010\u0007\u001a\u00020\bJ\u0016\u0010\t\u001a\u00020\b2\u0006\u0010\n\u001a\u00020\u000bH\u0086@\u00a2\u0006\u0002\u0010\fJ\u0016\u0010\r\u001a\u00020\b2\u0006\u0010\u000e\u001a\u00020\u000fH\u0086@\u00a2\u0006\u0002\u0010\u0010J\b\u0010\u0011\u001a\u00020\u0012H\u0002J\u0010\u0010\u0013\u001a\u0004\u0018\u00010\u0014H\u0086@\u00a2\u0006\u0002\u0010\u0015J\u001a\u0010\u0016\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u000b0\u00180\u0017H\u0086@\u00a2\u0006\u0002\u0010\u0015J\u001a\u0010\u0019\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00140\u00180\u0017H\u0086@\u00a2\u0006\u0002\u0010\u0015J\u000e\u0010\u001a\u001a\u00020\u0006H\u0082@\u00a2\u0006\u0002\u0010\u0015J\b\u0010\u001b\u001a\u00020\u001cH\u0002J\"\u0010\u001d\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u000f0\u00180\u00172\u0006\u0010\u001e\u001a\u00020\u001fH\u0086@\u00a2\u0006\u0002\u0010 J\u000e\u0010!\u001a\u00020\bH\u0086@\u00a2\u0006\u0002\u0010\u0015J\u0016\u0010\"\u001a\u00020\u001f2\u0006\u0010\n\u001a\u00020\u000bH\u0086@\u00a2\u0006\u0002\u0010\fJ\u0016\u0010#\u001a\u00020\u001f2\u0006\u0010\u000e\u001a\u00020\u000fH\u0086@\u00a2\u0006\u0002\u0010\u0010J\u0016\u0010$\u001a\u00020\b2\u0006\u0010%\u001a\u00020\u0014H\u0086@\u00a2\u0006\u0002\u0010&J\u0016\u0010\'\u001a\u00020\b2\u0006\u0010(\u001a\u00020\u0012H\u0086@\u00a2\u0006\u0002\u0010)R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0005\u001a\u0004\u0018\u00010\u0006X\u0082\u000e\u00a2\u0006\u0002\n\u0000\u00a8\u0006+"}, d2 = {"Lcom/aisoul/privateassistant/data/repository/AISoulRepository;", "", "context", "Landroid/content/Context;", "(Landroid/content/Context;)V", "database", "Lcom/aisoul/privateassistant/data/database/AISoulDatabase;", "closeDatabase", "", "deleteConversation", "conversation", "Lcom/aisoul/privateassistant/data/entities/Conversation;", "(Lcom/aisoul/privateassistant/data/entities/Conversation;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "deleteMessage", "message", "Lcom/aisoul/privateassistant/data/entities/Message;", "(Lcom/aisoul/privateassistant/data/entities/Message;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "generateSecurePassphrase", "", "getActiveModel", "Lcom/aisoul/privateassistant/data/entities/AIModel;", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getAllConversations", "Lkotlinx/coroutines/flow/Flow;", "", "getAllModels", "getDatabase", "getDatabasePassphrase", "", "getMessagesForConversation", "conversationId", "", "(JLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "initializeDefaultModels", "insertConversation", "insertMessage", "insertModel", "model", "(Lcom/aisoul/privateassistant/data/entities/AIModel;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "setActiveModel", "modelId", "(Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "Companion", "app_debug"})
public final class AISoulRepository {
    @org.jetbrains.annotations.NotNull()
    private final android.content.Context context = null;
    @org.jetbrains.annotations.Nullable()
    private com.aisoul.privateassistant.data.database.AISoulDatabase database;
    @kotlin.jvm.Volatile()
    @org.jetbrains.annotations.Nullable()
    private static volatile com.aisoul.privateassistant.data.repository.AISoulRepository INSTANCE;
    @org.jetbrains.annotations.NotNull()
    public static final com.aisoul.privateassistant.data.repository.AISoulRepository.Companion Companion = null;
    
    private AISoulRepository(android.content.Context context) {
        super();
    }
    
    private final java.lang.Object getDatabase(kotlin.coroutines.Continuation<? super com.aisoul.privateassistant.data.database.AISoulDatabase> $completion) {
        return null;
    }
    
    private final char[] getDatabasePassphrase() {
        return null;
    }
    
    private final java.lang.String generateSecurePassphrase() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object getAllConversations(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlinx.coroutines.flow.Flow<? extends java.util.List<com.aisoul.privateassistant.data.entities.Conversation>>> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object insertConversation(@org.jetbrains.annotations.NotNull()
    com.aisoul.privateassistant.data.entities.Conversation conversation, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.Long> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object deleteConversation(@org.jetbrains.annotations.NotNull()
    com.aisoul.privateassistant.data.entities.Conversation conversation, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object getMessagesForConversation(long conversationId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlinx.coroutines.flow.Flow<? extends java.util.List<com.aisoul.privateassistant.data.entities.Message>>> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object insertMessage(@org.jetbrains.annotations.NotNull()
    com.aisoul.privateassistant.data.entities.Message message, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.Long> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object deleteMessage(@org.jetbrains.annotations.NotNull()
    com.aisoul.privateassistant.data.entities.Message message, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object getAllModels(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlinx.coroutines.flow.Flow<? extends java.util.List<com.aisoul.privateassistant.data.entities.AIModel>>> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object getActiveModel(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.aisoul.privateassistant.data.entities.AIModel> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object setActiveModel(@org.jetbrains.annotations.NotNull()
    java.lang.String modelId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object insertModel(@org.jetbrains.annotations.NotNull()
    com.aisoul.privateassistant.data.entities.AIModel model, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object initializeDefaultModels(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    public final void closeDatabase() {
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u001a\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u000e\u0010\u0005\u001a\u00020\u00042\u0006\u0010\u0006\u001a\u00020\u0007R\u0010\u0010\u0003\u001a\u0004\u0018\u00010\u0004X\u0082\u000e\u00a2\u0006\u0002\n\u0000\u00a8\u0006\b"}, d2 = {"Lcom/aisoul/privateassistant/data/repository/AISoulRepository$Companion;", "", "()V", "INSTANCE", "Lcom/aisoul/privateassistant/data/repository/AISoulRepository;", "getInstance", "context", "Landroid/content/Context;", "app_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.aisoul.privateassistant.data.repository.AISoulRepository getInstance(@org.jetbrains.annotations.NotNull()
        android.content.Context context) {
            return null;
        }
    }
}