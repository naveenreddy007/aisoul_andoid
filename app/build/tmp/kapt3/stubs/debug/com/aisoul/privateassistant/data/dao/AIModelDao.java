package com.aisoul.privateassistant.data.dao;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00006\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0002\b\u0006\n\u0002\u0010\t\n\u0002\b\u0005\bg\u0018\u00002\u00020\u0001J\u000e\u0010\u0002\u001a\u00020\u0003H\u00a7@\u00a2\u0006\u0002\u0010\u0004J\u0016\u0010\u0005\u001a\u00020\u00032\u0006\u0010\u0006\u001a\u00020\u0007H\u00a7@\u00a2\u0006\u0002\u0010\bJ\u000e\u0010\t\u001a\u00020\u0003H\u00a7@\u00a2\u0006\u0002\u0010\u0004J\u0010\u0010\n\u001a\u0004\u0018\u00010\u0007H\u00a7@\u00a2\u0006\u0002\u0010\u0004J\u0014\u0010\u000b\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00070\r0\fH\'J\u0014\u0010\u000e\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00070\r0\fH\'J\u0018\u0010\u000f\u001a\u0004\u0018\u00010\u00072\u0006\u0010\u0010\u001a\u00020\u0011H\u00a7@\u00a2\u0006\u0002\u0010\u0012J\u0016\u0010\u0013\u001a\u00020\u00032\u0006\u0010\u0006\u001a\u00020\u0007H\u00a7@\u00a2\u0006\u0002\u0010\bJ.\u0010\u0014\u001a\u00020\u00032\u0006\u0010\u0015\u001a\u00020\u00112\u0006\u0010\u0016\u001a\u00020\u00112\u0006\u0010\u0017\u001a\u00020\u00182\u0006\u0010\u0019\u001a\u00020\u0011H\u00a7@\u00a2\u0006\u0002\u0010\u001aJ\u0016\u0010\u001b\u001a\u00020\u00032\u0006\u0010\u0015\u001a\u00020\u0011H\u00a7@\u00a2\u0006\u0002\u0010\u0012J\u0016\u0010\u001c\u001a\u00020\u00032\u0006\u0010\u0006\u001a\u00020\u0007H\u00a7@\u00a2\u0006\u0002\u0010\b\u00a8\u0006\u001d"}, d2 = {"Lcom/aisoul/privateassistant/data/dao/AIModelDao;", "", "deactivateAllModels", "", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "deleteModel", "model", "Lcom/aisoul/privateassistant/data/entities/AIModel;", "(Lcom/aisoul/privateassistant/data/entities/AIModel;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "deleteUndownloadedModels", "getActiveModel", "getAllModels", "Lkotlinx/coroutines/flow/Flow;", "", "getDownloadedModels", "getModelById", "id", "", "(Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "insertModel", "markModelAsDownloaded", "modelId", "path", "timestamp", "", "checksum", "(Ljava/lang/String;Ljava/lang/String;JLjava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "setActiveModel", "updateModel", "app_debug"})
@androidx.room.Dao()
public abstract interface AIModelDao {
    
    @androidx.room.Query(value = "SELECT * FROM ai_models ORDER BY name ASC")
    @org.jetbrains.annotations.NotNull()
    public abstract kotlinx.coroutines.flow.Flow<java.util.List<com.aisoul.privateassistant.data.entities.AIModel>> getAllModels();
    
    @androidx.room.Query(value = "SELECT * FROM ai_models WHERE isDownloaded = 1 ORDER BY name ASC")
    @org.jetbrains.annotations.NotNull()
    public abstract kotlinx.coroutines.flow.Flow<java.util.List<com.aisoul.privateassistant.data.entities.AIModel>> getDownloadedModels();
    
    @androidx.room.Query(value = "SELECT * FROM ai_models WHERE isActive = 1 LIMIT 1")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getActiveModel(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.aisoul.privateassistant.data.entities.AIModel> $completion);
    
    @androidx.room.Query(value = "SELECT * FROM ai_models WHERE id = :id")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getModelById(@org.jetbrains.annotations.NotNull()
    java.lang.String id, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.aisoul.privateassistant.data.entities.AIModel> $completion);
    
    @androidx.room.Insert(onConflict = 1)
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object insertModel(@org.jetbrains.annotations.NotNull()
    com.aisoul.privateassistant.data.entities.AIModel model, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Update()
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object updateModel(@org.jetbrains.annotations.NotNull()
    com.aisoul.privateassistant.data.entities.AIModel model, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Delete()
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object deleteModel(@org.jetbrains.annotations.NotNull()
    com.aisoul.privateassistant.data.entities.AIModel model, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Query(value = "UPDATE ai_models SET isActive = 0")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object deactivateAllModels(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Query(value = "UPDATE ai_models SET isActive = 1 WHERE id = :modelId")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object setActiveModel(@org.jetbrains.annotations.NotNull()
    java.lang.String modelId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Query(value = "UPDATE ai_models SET isDownloaded = 1, downloadPath = :path, downloadedAt = :timestamp, checksumSha256 = :checksum WHERE id = :modelId")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object markModelAsDownloaded(@org.jetbrains.annotations.NotNull()
    java.lang.String modelId, @org.jetbrains.annotations.NotNull()
    java.lang.String path, long timestamp, @org.jetbrains.annotations.NotNull()
    java.lang.String checksum, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Query(value = "DELETE FROM ai_models WHERE isDownloaded = 0")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object deleteUndownloadedModels(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
}