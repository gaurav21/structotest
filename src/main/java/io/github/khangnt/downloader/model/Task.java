package io.github.khangnt.downloader.model;

import static io.github.khangnt.downloader.C.DEFAULT_MAX_PARALLEL_CONNECTIONS;
import static io.github.khangnt.downloader.C.UNSET;
import static io.github.khangnt.downloader.util.Utils.isEmpty;

/**
 * Created by Khang NT on 6/2/17.
 * Email: khang.neon.1997@gmail.com
 */

public class Task {

    public enum State {
        IDLE,
        DOWNLOADING,
        WAITING,
        MERGING,
        FAILED,
        FINISHED;
    }


    private int mId = UNSET;
    private String mUrl;
    private String mFilePath;
    private long mLength = UNSET;
    private String mDeveloperPayload;
    private State mState = State.IDLE;
    private String message;
    private int mMaxParallelConnections = DEFAULT_MAX_PARALLEL_CONNECTIONS;

    private Task() {}

    public int getId() {
        return mId;
    }

    public String getUrl() {
        return mUrl;
    }

    public String getFilePath() {
        return mFilePath;
    }

    public long getLength() {
        return mLength;
    }

    public boolean isResumable() {
        return mLength > 0;
    }

    public String getDeveloperPayload() {
        return mDeveloperPayload;
    }

    public State getState() {
        return mState;
    }

    public String getMessage() {
        return message;
    }

    public int getMaxParallelConnections() {
        return mMaxParallelConnections;
    }

    public Builder newBuilder() {
        return new Builder(getFilePath(), getUrl())
                .setId(getId())
                .setDeveloperPayload(getDeveloperPayload())
                .setLength(getLength())
                .setMessage(getMessage())
                .setState(getState())
                .setMaxParallelConnections(getMaxParallelConnections());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Task task = (Task) o;

        if (mId != task.mId) return false;
        if (!mUrl.equals(task.mUrl)) return false;
        return mFilePath.equals(task.mFilePath);

    }

    @Override
    public int hashCode() {
        int result = mId;
        result = 31 * result + mUrl.hashCode();
        result = 31 * result + mFilePath.hashCode();
        return result;
    }

    public static class Builder {
        private Task mTask = new Task();

        public Builder(String filePath, String url) {
            if (isEmpty(filePath) || isEmpty(url))
                throw new IllegalArgumentException("File path and url can't be empty");
            mTask.mFilePath = filePath;
            mTask.mUrl = url;
        }

        public Builder setId(int id) {
            mTask.mId = id;
            return this;
        }

        public Builder setDeveloperPayload(String developerPayload) {
            mTask.mDeveloperPayload = developerPayload;
            return this;
        }

        public Builder setLength(long length) {
            mTask.mLength = length;
            return this;
        }

        public Builder setState(State state) {
            mTask.mState = state;
            return this;
        }

        public Builder setMessage(String message) {
            mTask.message = message;
            return this;
        }

        public Builder setMaxParallelConnections(int n) {
            if (n <= 0) throw new IllegalArgumentException("Max chunk can't < 0");
            mTask.mMaxParallelConnections = n;
            return this;
        }

        public int getId() {
            return mTask.mId;
        }

        public String getUrl() {
            return mTask.mUrl;
        }

        public String getFilePath() {
            return mTask.mFilePath;
        }

        public long getLength() {
            return mTask.mLength;
        }

        public boolean isResumable() {
            return mTask.mLength > 0;
        }

        public String getDeveloperPayload() {
            return mTask.mDeveloperPayload;
        }

        public State getState() {
            return mTask.mState;
        }

        public String getMessage() {
            return mTask.message;
        }

        public int getMaxChunks() {
            return mTask.mMaxParallelConnections;
        }

        public Task build() {
            return mTask;
        }
    }
}
