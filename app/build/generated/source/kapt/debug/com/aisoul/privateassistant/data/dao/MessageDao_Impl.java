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
import com.aisoul.privateassistant.data.entities.Message;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Integer;
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
public final class MessageDao_Impl implements MessageDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<Message> __insertionAdapterOfMessage;

  private final EntityDeletionOrUpdateAdapter<Message> __deletionAdapterOfMessage;

  private final EntityDeletionOrUpdateAdapter<Message> __updateAdapterOfMessage;

  private final SharedSQLiteStatement __preparedStmtOfDeleteMessagesForConversation;

  public MessageDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfMessage = new EntityInsertionAdapter<Message>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `messages` (`id`,`conversationId`,`content`,`isFromUser`,`timestamp`,`modelUsed`,`processingTimeMs`) VALUES (nullif(?, 0),?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final Message entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getConversationId());
        if (entity.getContent() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getContent());
        }
        final int _tmp = entity.isFromUser() ? 1 : 0;
        statement.bindLong(4, _tmp);
        statement.bindLong(5, entity.getTimestamp());
        if (entity.getModelUsed() == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.getModelUsed());
        }
        if (entity.getProcessingTimeMs() == null) {
          statement.bindNull(7);
        } else {
          statement.bindLong(7, entity.getProcessingTimeMs());
        }
      }
    };
    this.__deletionAdapterOfMessage = new EntityDeletionOrUpdateAdapter<Message>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `messages` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final Message entity) {
        statement.bindLong(1, entity.getId());
      }
    };
    this.__updateAdapterOfMessage = new EntityDeletionOrUpdateAdapter<Message>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `messages` SET `id` = ?,`conversationId` = ?,`content` = ?,`isFromUser` = ?,`timestamp` = ?,`modelUsed` = ?,`processingTimeMs` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final Message entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getConversationId());
        if (entity.getContent() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getContent());
        }
        final int _tmp = entity.isFromUser() ? 1 : 0;
        statement.bindLong(4, _tmp);
        statement.bindLong(5, entity.getTimestamp());
        if (entity.getModelUsed() == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.getModelUsed());
        }
        if (entity.getProcessingTimeMs() == null) {
          statement.bindNull(7);
        } else {
          statement.bindLong(7, entity.getProcessingTimeMs());
        }
        statement.bindLong(8, entity.getId());
      }
    };
    this.__preparedStmtOfDeleteMessagesForConversation = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM messages WHERE conversationId = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insertMessage(final Message message, final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfMessage.insertAndReturnId(message);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteMessage(final Message message, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfMessage.handle(message);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateMessage(final Message message, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfMessage.handle(message);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteMessagesForConversation(final long conversationId,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteMessagesForConversation.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, conversationId);
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
          __preparedStmtOfDeleteMessagesForConversation.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<Message>> getMessagesForConversation(final long conversationId) {
    final String _sql = "SELECT * FROM messages WHERE conversationId = ? ORDER BY timestamp ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, conversationId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"messages"}, new Callable<List<Message>>() {
      @Override
      @NonNull
      public List<Message> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfConversationId = CursorUtil.getColumnIndexOrThrow(_cursor, "conversationId");
          final int _cursorIndexOfContent = CursorUtil.getColumnIndexOrThrow(_cursor, "content");
          final int _cursorIndexOfIsFromUser = CursorUtil.getColumnIndexOrThrow(_cursor, "isFromUser");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfModelUsed = CursorUtil.getColumnIndexOrThrow(_cursor, "modelUsed");
          final int _cursorIndexOfProcessingTimeMs = CursorUtil.getColumnIndexOrThrow(_cursor, "processingTimeMs");
          final List<Message> _result = new ArrayList<Message>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Message _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpConversationId;
            _tmpConversationId = _cursor.getLong(_cursorIndexOfConversationId);
            final String _tmpContent;
            if (_cursor.isNull(_cursorIndexOfContent)) {
              _tmpContent = null;
            } else {
              _tmpContent = _cursor.getString(_cursorIndexOfContent);
            }
            final boolean _tmpIsFromUser;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsFromUser);
            _tmpIsFromUser = _tmp != 0;
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final String _tmpModelUsed;
            if (_cursor.isNull(_cursorIndexOfModelUsed)) {
              _tmpModelUsed = null;
            } else {
              _tmpModelUsed = _cursor.getString(_cursorIndexOfModelUsed);
            }
            final Long _tmpProcessingTimeMs;
            if (_cursor.isNull(_cursorIndexOfProcessingTimeMs)) {
              _tmpProcessingTimeMs = null;
            } else {
              _tmpProcessingTimeMs = _cursor.getLong(_cursorIndexOfProcessingTimeMs);
            }
            _item = new Message(_tmpId,_tmpConversationId,_tmpContent,_tmpIsFromUser,_tmpTimestamp,_tmpModelUsed,_tmpProcessingTimeMs);
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
  public Object getMessageById(final long id, final Continuation<? super Message> $completion) {
    final String _sql = "SELECT * FROM messages WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, id);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Message>() {
      @Override
      @Nullable
      public Message call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfConversationId = CursorUtil.getColumnIndexOrThrow(_cursor, "conversationId");
          final int _cursorIndexOfContent = CursorUtil.getColumnIndexOrThrow(_cursor, "content");
          final int _cursorIndexOfIsFromUser = CursorUtil.getColumnIndexOrThrow(_cursor, "isFromUser");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfModelUsed = CursorUtil.getColumnIndexOrThrow(_cursor, "modelUsed");
          final int _cursorIndexOfProcessingTimeMs = CursorUtil.getColumnIndexOrThrow(_cursor, "processingTimeMs");
          final Message _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpConversationId;
            _tmpConversationId = _cursor.getLong(_cursorIndexOfConversationId);
            final String _tmpContent;
            if (_cursor.isNull(_cursorIndexOfContent)) {
              _tmpContent = null;
            } else {
              _tmpContent = _cursor.getString(_cursorIndexOfContent);
            }
            final boolean _tmpIsFromUser;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsFromUser);
            _tmpIsFromUser = _tmp != 0;
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final String _tmpModelUsed;
            if (_cursor.isNull(_cursorIndexOfModelUsed)) {
              _tmpModelUsed = null;
            } else {
              _tmpModelUsed = _cursor.getString(_cursorIndexOfModelUsed);
            }
            final Long _tmpProcessingTimeMs;
            if (_cursor.isNull(_cursorIndexOfProcessingTimeMs)) {
              _tmpProcessingTimeMs = null;
            } else {
              _tmpProcessingTimeMs = _cursor.getLong(_cursorIndexOfProcessingTimeMs);
            }
            _result = new Message(_tmpId,_tmpConversationId,_tmpContent,_tmpIsFromUser,_tmpTimestamp,_tmpModelUsed,_tmpProcessingTimeMs);
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
  public Object getMessageCountForConversation(final long conversationId,
      final Continuation<? super Integer> $completion) {
    final String _sql = "SELECT COUNT(*) FROM messages WHERE conversationId = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, conversationId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Integer>() {
      @Override
      @NonNull
      public Integer call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Integer _result;
          if (_cursor.moveToFirst()) {
            final Integer _tmp;
            if (_cursor.isNull(0)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getInt(0);
            }
            _result = _tmp;
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
  public Object getLastMessageForConversation(final long conversationId,
      final Continuation<? super Message> $completion) {
    final String _sql = "SELECT * FROM messages WHERE conversationId = ? ORDER BY timestamp DESC LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, conversationId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Message>() {
      @Override
      @Nullable
      public Message call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfConversationId = CursorUtil.getColumnIndexOrThrow(_cursor, "conversationId");
          final int _cursorIndexOfContent = CursorUtil.getColumnIndexOrThrow(_cursor, "content");
          final int _cursorIndexOfIsFromUser = CursorUtil.getColumnIndexOrThrow(_cursor, "isFromUser");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfModelUsed = CursorUtil.getColumnIndexOrThrow(_cursor, "modelUsed");
          final int _cursorIndexOfProcessingTimeMs = CursorUtil.getColumnIndexOrThrow(_cursor, "processingTimeMs");
          final Message _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpConversationId;
            _tmpConversationId = _cursor.getLong(_cursorIndexOfConversationId);
            final String _tmpContent;
            if (_cursor.isNull(_cursorIndexOfContent)) {
              _tmpContent = null;
            } else {
              _tmpContent = _cursor.getString(_cursorIndexOfContent);
            }
            final boolean _tmpIsFromUser;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsFromUser);
            _tmpIsFromUser = _tmp != 0;
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final String _tmpModelUsed;
            if (_cursor.isNull(_cursorIndexOfModelUsed)) {
              _tmpModelUsed = null;
            } else {
              _tmpModelUsed = _cursor.getString(_cursorIndexOfModelUsed);
            }
            final Long _tmpProcessingTimeMs;
            if (_cursor.isNull(_cursorIndexOfProcessingTimeMs)) {
              _tmpProcessingTimeMs = null;
            } else {
              _tmpProcessingTimeMs = _cursor.getLong(_cursorIndexOfProcessingTimeMs);
            }
            _result = new Message(_tmpId,_tmpConversationId,_tmpContent,_tmpIsFromUser,_tmpTimestamp,_tmpModelUsed,_tmpProcessingTimeMs);
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
  public Flow<List<Message>> searchMessages(final String searchTerm) {
    final String _sql = "SELECT * FROM messages WHERE content LIKE '%' || ? || '%' ORDER BY timestamp DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    if (searchTerm == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, searchTerm);
    }
    return CoroutinesRoom.createFlow(__db, false, new String[] {"messages"}, new Callable<List<Message>>() {
      @Override
      @NonNull
      public List<Message> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfConversationId = CursorUtil.getColumnIndexOrThrow(_cursor, "conversationId");
          final int _cursorIndexOfContent = CursorUtil.getColumnIndexOrThrow(_cursor, "content");
          final int _cursorIndexOfIsFromUser = CursorUtil.getColumnIndexOrThrow(_cursor, "isFromUser");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfModelUsed = CursorUtil.getColumnIndexOrThrow(_cursor, "modelUsed");
          final int _cursorIndexOfProcessingTimeMs = CursorUtil.getColumnIndexOrThrow(_cursor, "processingTimeMs");
          final List<Message> _result = new ArrayList<Message>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Message _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpConversationId;
            _tmpConversationId = _cursor.getLong(_cursorIndexOfConversationId);
            final String _tmpContent;
            if (_cursor.isNull(_cursorIndexOfContent)) {
              _tmpContent = null;
            } else {
              _tmpContent = _cursor.getString(_cursorIndexOfContent);
            }
            final boolean _tmpIsFromUser;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsFromUser);
            _tmpIsFromUser = _tmp != 0;
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final String _tmpModelUsed;
            if (_cursor.isNull(_cursorIndexOfModelUsed)) {
              _tmpModelUsed = null;
            } else {
              _tmpModelUsed = _cursor.getString(_cursorIndexOfModelUsed);
            }
            final Long _tmpProcessingTimeMs;
            if (_cursor.isNull(_cursorIndexOfProcessingTimeMs)) {
              _tmpProcessingTimeMs = null;
            } else {
              _tmpProcessingTimeMs = _cursor.getLong(_cursorIndexOfProcessingTimeMs);
            }
            _item = new Message(_tmpId,_tmpConversationId,_tmpContent,_tmpIsFromUser,_tmpTimestamp,_tmpModelUsed,_tmpProcessingTimeMs);
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

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
