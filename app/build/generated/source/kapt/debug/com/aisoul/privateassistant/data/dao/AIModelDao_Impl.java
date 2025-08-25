package com.aisoul.privateassistant.data.dao;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.aisoul.privateassistant.data.entities.AIModel;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class AIModelDao_Impl implements AIModelDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<AIModel> __insertionAdapterOfAIModel;

  private final EntityDeletionOrUpdateAdapter<AIModel> __deletionAdapterOfAIModel;

  private final EntityDeletionOrUpdateAdapter<AIModel> __updateAdapterOfAIModel;

  private final SharedSQLiteStatement __preparedStmtOfDeactivateAllModels;

  private final SharedSQLiteStatement __preparedStmtOfSetActiveModel;

  private final SharedSQLiteStatement __preparedStmtOfMarkModelAsDownloaded;

  private final SharedSQLiteStatement __preparedStmtOfDeleteUndownloadedModels;

  public AIModelDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfAIModel = new EntityInsertionAdapter<AIModel>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `ai_models` (`id`,`name`,`description`,`sizeBytes`,`minRamMB`,`minStorageMB`,`isDownloaded`,`downloadPath`,`downloadedAt`,`checksumSha256`,`version`,`isActive`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final AIModel entity) {
        if (entity.getId() == null) {
          statement.bindNull(1);
        } else {
          statement.bindString(1, entity.getId());
        }
        if (entity.getName() == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.getName());
        }
        if (entity.getDescription() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getDescription());
        }
        statement.bindLong(4, entity.getSizeBytes());
        statement.bindLong(5, entity.getMinRamMB());
        statement.bindLong(6, entity.getMinStorageMB());
        final int _tmp = entity.isDownloaded() ? 1 : 0;
        statement.bindLong(7, _tmp);
        if (entity.getDownloadPath() == null) {
          statement.bindNull(8);
        } else {
          statement.bindString(8, entity.getDownloadPath());
        }
        if (entity.getDownloadedAt() == null) {
          statement.bindNull(9);
        } else {
          statement.bindLong(9, entity.getDownloadedAt());
        }
        if (entity.getChecksumSha256() == null) {
          statement.bindNull(10);
        } else {
          statement.bindString(10, entity.getChecksumSha256());
        }
        if (entity.getVersion() == null) {
          statement.bindNull(11);
        } else {
          statement.bindString(11, entity.getVersion());
        }
        final int _tmp_1 = entity.isActive() ? 1 : 0;
        statement.bindLong(12, _tmp_1);
      }
    };
    this.__deletionAdapterOfAIModel = new EntityDeletionOrUpdateAdapter<AIModel>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `ai_models` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final AIModel entity) {
        if (entity.getId() == null) {
          statement.bindNull(1);
        } else {
          statement.bindString(1, entity.getId());
        }
      }
    };
    this.__updateAdapterOfAIModel = new EntityDeletionOrUpdateAdapter<AIModel>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `ai_models` SET `id` = ?,`name` = ?,`description` = ?,`sizeBytes` = ?,`minRamMB` = ?,`minStorageMB` = ?,`isDownloaded` = ?,`downloadPath` = ?,`downloadedAt` = ?,`checksumSha256` = ?,`version` = ?,`isActive` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final AIModel entity) {
        if (entity.getId() == null) {
          statement.bindNull(1);
        } else {
          statement.bindString(1, entity.getId());
        }
        if (entity.getName() == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.getName());
        }
        if (entity.getDescription() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getDescription());
        }
        statement.bindLong(4, entity.getSizeBytes());
        statement.bindLong(5, entity.getMinRamMB());
        statement.bindLong(6, entity.getMinStorageMB());
        final int _tmp = entity.isDownloaded() ? 1 : 0;
        statement.bindLong(7, _tmp);
        if (entity.getDownloadPath() == null) {
          statement.bindNull(8);
        } else {
          statement.bindString(8, entity.getDownloadPath());
        }
        if (entity.getDownloadedAt() == null) {
          statement.bindNull(9);
        } else {
          statement.bindLong(9, entity.getDownloadedAt());
        }
        if (entity.getChecksumSha256() == null) {
          statement.bindNull(10);
        } else {
          statement.bindString(10, entity.getChecksumSha256());
        }
        if (entity.getVersion() == null) {
          statement.bindNull(11);
        } else {
          statement.bindString(11, entity.getVersion());
        }
        final int _tmp_1 = entity.isActive() ? 1 : 0;
        statement.bindLong(12, _tmp_1);
        if (entity.getId() == null) {
          statement.bindNull(13);
        } else {
          statement.bindString(13, entity.getId());
        }
      }
    };
    this.__preparedStmtOfDeactivateAllModels = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE ai_models SET isActive = 0";
        return _query;
      }
    };
    this.__preparedStmtOfSetActiveModel = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE ai_models SET isActive = 1 WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfMarkModelAsDownloaded = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE ai_models SET isDownloaded = 1, downloadPath = ?, downloadedAt = ?, checksumSha256 = ? WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteUndownloadedModels = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM ai_models WHERE isDownloaded = 0";
        return _query;
      }
    };
  }

  @Override
  public Object insertModel(final AIModel model, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfAIModel.insert(model);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteModel(final AIModel model, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfAIModel.handle(model);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateModel(final AIModel model, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfAIModel.handle(model);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deactivateAllModels(final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeactivateAllModels.acquire();
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeactivateAllModels.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object setActiveModel(final String modelId, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfSetActiveModel.acquire();
        int _argIndex = 1;
        if (modelId == null) {
          _stmt.bindNull(_argIndex);
        } else {
          _stmt.bindString(_argIndex, modelId);
        }
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfSetActiveModel.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object markModelAsDownloaded(final String modelId, final String path, final long timestamp,
      final String checksum, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfMarkModelAsDownloaded.acquire();
        int _argIndex = 1;
        if (path == null) {
          _stmt.bindNull(_argIndex);
        } else {
          _stmt.bindString(_argIndex, path);
        }
        _argIndex = 2;
        _stmt.bindLong(_argIndex, timestamp);
        _argIndex = 3;
        if (checksum == null) {
          _stmt.bindNull(_argIndex);
        } else {
          _stmt.bindString(_argIndex, checksum);
        }
        _argIndex = 4;
        if (modelId == null) {
          _stmt.bindNull(_argIndex);
        } else {
          _stmt.bindString(_argIndex, modelId);
        }
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfMarkModelAsDownloaded.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteUndownloadedModels(final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteUndownloadedModels.acquire();
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeleteUndownloadedModels.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<AIModel>> getAllModels() {
    final String _sql = "SELECT * FROM ai_models ORDER BY name ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"ai_models"}, new Callable<List<AIModel>>() {
      @Override
      @NonNull
      public List<AIModel> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfSizeBytes = CursorUtil.getColumnIndexOrThrow(_cursor, "sizeBytes");
          final int _cursorIndexOfMinRamMB = CursorUtil.getColumnIndexOrThrow(_cursor, "minRamMB");
          final int _cursorIndexOfMinStorageMB = CursorUtil.getColumnIndexOrThrow(_cursor, "minStorageMB");
          final int _cursorIndexOfIsDownloaded = CursorUtil.getColumnIndexOrThrow(_cursor, "isDownloaded");
          final int _cursorIndexOfDownloadPath = CursorUtil.getColumnIndexOrThrow(_cursor, "downloadPath");
          final int _cursorIndexOfDownloadedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "downloadedAt");
          final int _cursorIndexOfChecksumSha256 = CursorUtil.getColumnIndexOrThrow(_cursor, "checksumSha256");
          final int _cursorIndexOfVersion = CursorUtil.getColumnIndexOrThrow(_cursor, "version");
          final int _cursorIndexOfIsActive = CursorUtil.getColumnIndexOrThrow(_cursor, "isActive");
          final List<AIModel> _result = new ArrayList<AIModel>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final AIModel _item;
            final String _tmpId;
            if (_cursor.isNull(_cursorIndexOfId)) {
              _tmpId = null;
            } else {
              _tmpId = _cursor.getString(_cursorIndexOfId);
            }
            final String _tmpName;
            if (_cursor.isNull(_cursorIndexOfName)) {
              _tmpName = null;
            } else {
              _tmpName = _cursor.getString(_cursorIndexOfName);
            }
            final String _tmpDescription;
            if (_cursor.isNull(_cursorIndexOfDescription)) {
              _tmpDescription = null;
            } else {
              _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            }
            final long _tmpSizeBytes;
            _tmpSizeBytes = _cursor.getLong(_cursorIndexOfSizeBytes);
            final int _tmpMinRamMB;
            _tmpMinRamMB = _cursor.getInt(_cursorIndexOfMinRamMB);
            final int _tmpMinStorageMB;
            _tmpMinStorageMB = _cursor.getInt(_cursorIndexOfMinStorageMB);
            final boolean _tmpIsDownloaded;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsDownloaded);
            _tmpIsDownloaded = _tmp != 0;
            final String _tmpDownloadPath;
            if (_cursor.isNull(_cursorIndexOfDownloadPath)) {
              _tmpDownloadPath = null;
            } else {
              _tmpDownloadPath = _cursor.getString(_cursorIndexOfDownloadPath);
            }
            final Long _tmpDownloadedAt;
            if (_cursor.isNull(_cursorIndexOfDownloadedAt)) {
              _tmpDownloadedAt = null;
            } else {
              _tmpDownloadedAt = _cursor.getLong(_cursorIndexOfDownloadedAt);
            }
            final String _tmpChecksumSha256;
            if (_cursor.isNull(_cursorIndexOfChecksumSha256)) {
              _tmpChecksumSha256 = null;
            } else {
              _tmpChecksumSha256 = _cursor.getString(_cursorIndexOfChecksumSha256);
            }
            final String _tmpVersion;
            if (_cursor.isNull(_cursorIndexOfVersion)) {
              _tmpVersion = null;
            } else {
              _tmpVersion = _cursor.getString(_cursorIndexOfVersion);
            }
            final boolean _tmpIsActive;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsActive);
            _tmpIsActive = _tmp_1 != 0;
            _item = new AIModel(_tmpId,_tmpName,_tmpDescription,_tmpSizeBytes,_tmpMinRamMB,_tmpMinStorageMB,_tmpIsDownloaded,_tmpDownloadPath,_tmpDownloadedAt,_tmpChecksumSha256,_tmpVersion,_tmpIsActive);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<List<AIModel>> getDownloadedModels() {
    final String _sql = "SELECT * FROM ai_models WHERE isDownloaded = 1 ORDER BY name ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"ai_models"}, new Callable<List<AIModel>>() {
      @Override
      @NonNull
      public List<AIModel> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfSizeBytes = CursorUtil.getColumnIndexOrThrow(_cursor, "sizeBytes");
          final int _cursorIndexOfMinRamMB = CursorUtil.getColumnIndexOrThrow(_cursor, "minRamMB");
          final int _cursorIndexOfMinStorageMB = CursorUtil.getColumnIndexOrThrow(_cursor, "minStorageMB");
          final int _cursorIndexOfIsDownloaded = CursorUtil.getColumnIndexOrThrow(_cursor, "isDownloaded");
          final int _cursorIndexOfDownloadPath = CursorUtil.getColumnIndexOrThrow(_cursor, "downloadPath");
          final int _cursorIndexOfDownloadedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "downloadedAt");
          final int _cursorIndexOfChecksumSha256 = CursorUtil.getColumnIndexOrThrow(_cursor, "checksumSha256");
          final int _cursorIndexOfVersion = CursorUtil.getColumnIndexOrThrow(_cursor, "version");
          final int _cursorIndexOfIsActive = CursorUtil.getColumnIndexOrThrow(_cursor, "isActive");
          final List<AIModel> _result = new ArrayList<AIModel>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final AIModel _item;
            final String _tmpId;
            if (_cursor.isNull(_cursorIndexOfId)) {
              _tmpId = null;
            } else {
              _tmpId = _cursor.getString(_cursorIndexOfId);
            }
            final String _tmpName;
            if (_cursor.isNull(_cursorIndexOfName)) {
              _tmpName = null;
            } else {
              _tmpName = _cursor.getString(_cursorIndexOfName);
            }
            final String _tmpDescription;
            if (_cursor.isNull(_cursorIndexOfDescription)) {
              _tmpDescription = null;
            } else {
              _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            }
            final long _tmpSizeBytes;
            _tmpSizeBytes = _cursor.getLong(_cursorIndexOfSizeBytes);
            final int _tmpMinRamMB;
            _tmpMinRamMB = _cursor.getInt(_cursorIndexOfMinRamMB);
            final int _tmpMinStorageMB;
            _tmpMinStorageMB = _cursor.getInt(_cursorIndexOfMinStorageMB);
            final boolean _tmpIsDownloaded;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsDownloaded);
            _tmpIsDownloaded = _tmp != 0;
            final String _tmpDownloadPath;
            if (_cursor.isNull(_cursorIndexOfDownloadPath)) {
              _tmpDownloadPath = null;
            } else {
              _tmpDownloadPath = _cursor.getString(_cursorIndexOfDownloadPath);
            }
            final Long _tmpDownloadedAt;
            if (_cursor.isNull(_cursorIndexOfDownloadedAt)) {
              _tmpDownloadedAt = null;
            } else {
              _tmpDownloadedAt = _cursor.getLong(_cursorIndexOfDownloadedAt);
            }
            final String _tmpChecksumSha256;
            if (_cursor.isNull(_cursorIndexOfChecksumSha256)) {
              _tmpChecksumSha256 = null;
            } else {
              _tmpChecksumSha256 = _cursor.getString(_cursorIndexOfChecksumSha256);
            }
            final String _tmpVersion;
            if (_cursor.isNull(_cursorIndexOfVersion)) {
              _tmpVersion = null;
            } else {
              _tmpVersion = _cursor.getString(_cursorIndexOfVersion);
            }
            final boolean _tmpIsActive;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsActive);
            _tmpIsActive = _tmp_1 != 0;
            _item = new AIModel(_tmpId,_tmpName,_tmpDescription,_tmpSizeBytes,_tmpMinRamMB,_tmpMinStorageMB,_tmpIsDownloaded,_tmpDownloadPath,_tmpDownloadedAt,_tmpChecksumSha256,_tmpVersion,_tmpIsActive);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getActiveModel(final Continuation<? super AIModel> $completion) {
    final String _sql = "SELECT * FROM ai_models WHERE isActive = 1 LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<AIModel>() {
      @Override
      @Nullable
      public AIModel call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfSizeBytes = CursorUtil.getColumnIndexOrThrow(_cursor, "sizeBytes");
          final int _cursorIndexOfMinRamMB = CursorUtil.getColumnIndexOrThrow(_cursor, "minRamMB");
          final int _cursorIndexOfMinStorageMB = CursorUtil.getColumnIndexOrThrow(_cursor, "minStorageMB");
          final int _cursorIndexOfIsDownloaded = CursorUtil.getColumnIndexOrThrow(_cursor, "isDownloaded");
          final int _cursorIndexOfDownloadPath = CursorUtil.getColumnIndexOrThrow(_cursor, "downloadPath");
          final int _cursorIndexOfDownloadedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "downloadedAt");
          final int _cursorIndexOfChecksumSha256 = CursorUtil.getColumnIndexOrThrow(_cursor, "checksumSha256");
          final int _cursorIndexOfVersion = CursorUtil.getColumnIndexOrThrow(_cursor, "version");
          final int _cursorIndexOfIsActive = CursorUtil.getColumnIndexOrThrow(_cursor, "isActive");
          final AIModel _result;
          if (_cursor.moveToFirst()) {
            final String _tmpId;
            if (_cursor.isNull(_cursorIndexOfId)) {
              _tmpId = null;
            } else {
              _tmpId = _cursor.getString(_cursorIndexOfId);
            }
            final String _tmpName;
            if (_cursor.isNull(_cursorIndexOfName)) {
              _tmpName = null;
            } else {
              _tmpName = _cursor.getString(_cursorIndexOfName);
            }
            final String _tmpDescription;
            if (_cursor.isNull(_cursorIndexOfDescription)) {
              _tmpDescription = null;
            } else {
              _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            }
            final long _tmpSizeBytes;
            _tmpSizeBytes = _cursor.getLong(_cursorIndexOfSizeBytes);
            final int _tmpMinRamMB;
            _tmpMinRamMB = _cursor.getInt(_cursorIndexOfMinRamMB);
            final int _tmpMinStorageMB;
            _tmpMinStorageMB = _cursor.getInt(_cursorIndexOfMinStorageMB);
            final boolean _tmpIsDownloaded;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsDownloaded);
            _tmpIsDownloaded = _tmp != 0;
            final String _tmpDownloadPath;
            if (_cursor.isNull(_cursorIndexOfDownloadPath)) {
              _tmpDownloadPath = null;
            } else {
              _tmpDownloadPath = _cursor.getString(_cursorIndexOfDownloadPath);
            }
            final Long _tmpDownloadedAt;
            if (_cursor.isNull(_cursorIndexOfDownloadedAt)) {
              _tmpDownloadedAt = null;
            } else {
              _tmpDownloadedAt = _cursor.getLong(_cursorIndexOfDownloadedAt);
            }
            final String _tmpChecksumSha256;
            if (_cursor.isNull(_cursorIndexOfChecksumSha256)) {
              _tmpChecksumSha256 = null;
            } else {
              _tmpChecksumSha256 = _cursor.getString(_cursorIndexOfChecksumSha256);
            }
            final String _tmpVersion;
            if (_cursor.isNull(_cursorIndexOfVersion)) {
              _tmpVersion = null;
            } else {
              _tmpVersion = _cursor.getString(_cursorIndexOfVersion);
            }
            final boolean _tmpIsActive;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsActive);
            _tmpIsActive = _tmp_1 != 0;
            _result = new AIModel(_tmpId,_tmpName,_tmpDescription,_tmpSizeBytes,_tmpMinRamMB,_tmpMinStorageMB,_tmpIsDownloaded,_tmpDownloadPath,_tmpDownloadedAt,_tmpChecksumSha256,_tmpVersion,_tmpIsActive);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getModelById(final String id, final Continuation<? super AIModel> $completion) {
    final String _sql = "SELECT * FROM ai_models WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    if (id == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, id);
    }
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<AIModel>() {
      @Override
      @Nullable
      public AIModel call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfSizeBytes = CursorUtil.getColumnIndexOrThrow(_cursor, "sizeBytes");
          final int _cursorIndexOfMinRamMB = CursorUtil.getColumnIndexOrThrow(_cursor, "minRamMB");
          final int _cursorIndexOfMinStorageMB = CursorUtil.getColumnIndexOrThrow(_cursor, "minStorageMB");
          final int _cursorIndexOfIsDownloaded = CursorUtil.getColumnIndexOrThrow(_cursor, "isDownloaded");
          final int _cursorIndexOfDownloadPath = CursorUtil.getColumnIndexOrThrow(_cursor, "downloadPath");
          final int _cursorIndexOfDownloadedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "downloadedAt");
          final int _cursorIndexOfChecksumSha256 = CursorUtil.getColumnIndexOrThrow(_cursor, "checksumSha256");
          final int _cursorIndexOfVersion = CursorUtil.getColumnIndexOrThrow(_cursor, "version");
          final int _cursorIndexOfIsActive = CursorUtil.getColumnIndexOrThrow(_cursor, "isActive");
          final AIModel _result;
          if (_cursor.moveToFirst()) {
            final String _tmpId;
            if (_cursor.isNull(_cursorIndexOfId)) {
              _tmpId = null;
            } else {
              _tmpId = _cursor.getString(_cursorIndexOfId);
            }
            final String _tmpName;
            if (_cursor.isNull(_cursorIndexOfName)) {
              _tmpName = null;
            } else {
              _tmpName = _cursor.getString(_cursorIndexOfName);
            }
            final String _tmpDescription;
            if (_cursor.isNull(_cursorIndexOfDescription)) {
              _tmpDescription = null;
            } else {
              _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            }
            final long _tmpSizeBytes;
            _tmpSizeBytes = _cursor.getLong(_cursorIndexOfSizeBytes);
            final int _tmpMinRamMB;
            _tmpMinRamMB = _cursor.getInt(_cursorIndexOfMinRamMB);
            final int _tmpMinStorageMB;
            _tmpMinStorageMB = _cursor.getInt(_cursorIndexOfMinStorageMB);
            final boolean _tmpIsDownloaded;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsDownloaded);
            _tmpIsDownloaded = _tmp != 0;
            final String _tmpDownloadPath;
            if (_cursor.isNull(_cursorIndexOfDownloadPath)) {
              _tmpDownloadPath = null;
            } else {
              _tmpDownloadPath = _cursor.getString(_cursorIndexOfDownloadPath);
            }
            final Long _tmpDownloadedAt;
            if (_cursor.isNull(_cursorIndexOfDownloadedAt)) {
              _tmpDownloadedAt = null;
            } else {
              _tmpDownloadedAt = _cursor.getLong(_cursorIndexOfDownloadedAt);
            }
            final String _tmpChecksumSha256;
            if (_cursor.isNull(_cursorIndexOfChecksumSha256)) {
              _tmpChecksumSha256 = null;
            } else {
              _tmpChecksumSha256 = _cursor.getString(_cursorIndexOfChecksumSha256);
            }
            final String _tmpVersion;
            if (_cursor.isNull(_cursorIndexOfVersion)) {
              _tmpVersion = null;
            } else {
              _tmpVersion = _cursor.getString(_cursorIndexOfVersion);
            }
            final boolean _tmpIsActive;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsActive);
            _tmpIsActive = _tmp_1 != 0;
            _result = new AIModel(_tmpId,_tmpName,_tmpDescription,_tmpSizeBytes,_tmpMinRamMB,_tmpMinStorageMB,_tmpIsDownloaded,_tmpDownloadPath,_tmpDownloadedAt,_tmpChecksumSha256,_tmpVersion,_tmpIsActive);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
