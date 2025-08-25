package com.aisoul.privateassistant.data.dao;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000,\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\t\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\b\u0007\bg\u0018\u00002\u00020\u0001J\u0016\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u0005H\u00a7@\u00a2\u0006\u0002\u0010\u0006J\u000e\u0010\u0007\u001a\u00020\u0003H\u00a7@\u00a2\u0006\u0002\u0010\bJ\u0016\u0010\t\u001a\u00020\u00032\u0006\u0010\n\u001a\u00020\u000bH\u00a7@\u00a2\u0006\u0002\u0010\fJ\u0014\u0010\r\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u000b0\u000f0\u000eH\'J\u0018\u0010\u0010\u001a\u0004\u0018\u00010\u000b2\u0006\u0010\u0004\u001a\u00020\u0005H\u00a7@\u00a2\u0006\u0002\u0010\u0006J \u0010\u0011\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u00052\b\b\u0002\u0010\u0012\u001a\u00020\u0005H\u00a7@\u00a2\u0006\u0002\u0010\u0013J\u0016\u0010\u0014\u001a\u00020\u00052\u0006\u0010\n\u001a\u00020\u000bH\u00a7@\u00a2\u0006\u0002\u0010\fJ\u0016\u0010\u0015\u001a\u00020\u00032\u0006\u0010\n\u001a\u00020\u000bH\u00a7@\u00a2\u0006\u0002\u0010\f\u00a8\u0006\u0016"}, d2 = {"Lcom/aisoul/privateassistant/data/dao/ConversationDao;", "", "archiveConversation", "", "id", "", "(JLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "deleteArchivedConversations", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "deleteConversation", "conversation", "Lcom/aisoul/privateassistant/data/entities/Conversation;", "(Lcom/aisoul/privateassistant/data/entities/Conversation;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getAllConversations", "Lkotlinx/coroutines/flow/Flow;", "", "getConversationById", "incrementMessageCount", "timestamp", "(JJLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "insertConversation", "updateConversation", "app_debug"})
@androidx.room.Dao()
public abstract interface ConversationDao {
    
    @androidx.room.Query(value = "SELECT * FROM conversations ORDER BY updatedAt DESC")
    @org.jetbrains.annotations.NotNull()
    public abstract kotlinx.coroutines.flow.Flow<java.util.List<com.aisoul.privateassistant.data.entities.Conversation>> getAllConversations();
    
    @androidx.room.Query(value = "SELECT * FROM conversations WHERE id = :id")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getConversationById(long id, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.aisoul.privateassistant.data.entities.Conversation> $completion);
    
    @androidx.room.Insert()
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object insertConversation(@org.jetbrains.annotations.NotNull()
    com.aisoul.privateassistant.data.entities.Conversation conversation, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.Long> $completion);
    
    @androidx.room.Update()
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object updateConversation(@org.jetbrains.annotations.NotNull()
    com.aisoul.privateassistant.data.entities.Conversation conversation, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Delete()
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object deleteConversation(@org.jetbrains.annotations.NotNull()
    com.aisoul.privateassistant.data.entities.Conversation conversation, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Query(value = "DELETE FROM conversations WHERE isArchived = 1")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object deleteArchivedConversations(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Query(value = "UPDATE conversations SET isArchived = 1 WHERE id = :id")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object archiveConversation(long id, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Query(value = "UPDATE conversations SET messageCount = messageCount + 1, updatedAt = :timestamp WHERE id = :id")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object incrementMessageCount(long id, long timestamp, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 3, xi = 48)
    public static final class DefaultImpls {
    }
}