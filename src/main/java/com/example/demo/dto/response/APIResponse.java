package com.example.demo.dto.response;

public class APIResponse<T> {
    private int code;
    private String message;
    private T result;

    public APIResponse() {
    }

    public APIResponse(int code, String message, T result) {
        this.code = code;
        this.message = message;
        this.result = result;
    }   

    public static <T> Builder<T> builder() {
        return new Builder<>();
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getResult() {
        return result;
    }

    public void setResult(T result) {
        this.result = result;
    }

    public static class Builder<T> {
        private int code;
        private String message;
        private T result;

        public Builder<T> code(int code) {
            this.code = code;
            return this;
        }

        public Builder<T> message(String message) {
            this.message = message;
            return this;
        }

        public Builder<T> result(T result) {
            this.result = result;
            return this;
        }

        public APIResponse<T> build() {
            return new APIResponse<>(code, message, result);
        }
    }
}
