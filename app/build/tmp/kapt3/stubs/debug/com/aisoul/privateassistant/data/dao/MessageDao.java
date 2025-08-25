package com.aisoul.privateassistant.data.dao;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000:\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\t\n\u0002\b\u0005\n\u0002\u0010\b\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0002\b\u0002\bg\u0018\u00002\u00020\u0001J\u0016\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u0005H\u00a7@\u00a2\u0006\u0002\u0010\u0006J\u0016\u0010\u0007\u001a\u00020\u00032\u0006\u0010\b\u001a\u00020\tH\u00a7@\u00a2\u0006\u0002\u0010\nJ\u0018\u0010\u000b\u001a\u0004\u0018\u00010\u00052\u0006\u0010\b\u001a\u00020\tH\u00a7@\u00a2\u0006\u0002\u0010\nJ\u0018\u0010\f\u001a\u0004\u0018\u00010\u00052\u0006\u0010\r\u001a\u00020\tH\u00a7@\u00a2\u0006\u0002\u0010\nJ\u0016\u0010\u000e\u001a\u00020\u000f2\u0006\u0010\b\u001a\u00020\tH\u00a7@\u00a2\u0006\u0002\u0010\nJ\u001c\u0010\u0010\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00050\u00120\u00112\u0006\u0010\b\u001a\u00020\tH\'J\u0016\u0010\u0013\u001a\u00020\t2\u0006\u0010\u0004\u001a\u00020\u0005H\u00a7@\u00a2\u0006\u0002\u0010\u0006J\u001c\u0010\u0014\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00050\u00120\u00112\u0006\u0010\u0015\u001a\u00020\u0016H\'J\u0016\u0010\u0017\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u0005H\u00a7@\u00a2\u0006\u0002\u0010\u0006\u00a8\u0006\u0018"}, d2 = {"Lcom/aisoul/privateassistant/data/dao/MessageDao;", "", "deleteMessage", "", "message", "Lcom/aisoul/privateassistant/data/entities/Message;", "(Lcom/aisoul/privateassistant/data/entities/Message;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "deleteMessagesForConversation", "conversationId", "", "(JLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getLastMessageForConversation", "getMessageById", "id", "getMessageCountForConversation", "", "getMessagesForConversation", "Lkotlinx/coroutines/flow/Flow;", "", "insertMessage", "searchMessages", "searchTerm", "", "updateMessage", "app_debug"})
@androidx.room.Dao()
public abstract interface MessageDao {
    
    @androidx.room.Query(value = "SELECT * FROM messages WHERE conversationId = :conversationId ORDER BY timestamp ASC")
    @org.jetbrains.annotations.NotNull()
    public abstract kotlinx.coroutines.flow.Flow<java.util.List<com.aisoul.privateassistant.data.entities.Message>> getMessagesForConversation(long conversationId);
    
    @androidx.room.Query(value = "SELECT * FROM messages WHERE id = :id")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getMessageById(long id, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.aisoul.privateassistant.data.entities.Message> $completion);
    
    @androidx.room.Insert()
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object insertMessage(@org.jetbrains.annotations.NotNull()
    com.aisoul.privateassistant.data.entities.Message message, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.Long> $completion);
    
    @androidx.room.Update()
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object updateMessage(@org.jetbrains.annotations.NotNull()
    com.aisoul.privateassistant.data.entities.Message message, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Delete()
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object deleteMessage(@org.jetbrains.annotations.NotNull()
    com.aisoul.privateassistant.data.entities.Message message, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Query(value = "DELETE FROM messages WHERE conversationId = :conversationId")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object deleteMessagesForConversation(long conversationId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Query(value = "SELECT COUNT(*) FROM messages WHERE conversationId = :conversationId")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getMessageCountForConversation(long conversationId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.Integer> $completion);
    
    @androidx.room.Query(value = "SELECT * FROM messages WHERE conversationId = :conversationId ORDER BY timestamp DESC LIMIT 1")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getLastMessageForConversation(long conversationId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.aisoul.privateassistant.data.entities.Message> $completion);
    
    @androidx.room.Query(value = "SELECT * FROM messages WHERE content LIKE \'%\' || :searchTerm || \'%\' ORDER BY timestamp DESC")
    @org.jetbrains.annotations.NotNull()
    public abstract kotlinx.coroutines.flow.Flow<java.util.List<com.aisoul.privateassistant.data.entities.Message>> searchMessages(@org.jetbrains.annotations.NotNull()
    java.lang.String searchTerm);
}