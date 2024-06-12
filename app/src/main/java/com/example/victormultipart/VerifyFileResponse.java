package com.example.victormultipart;

public class VerifyFileResponse {
    private boolean success;
    private VerifySkillProvider verifySkillProvider;


    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public VerifySkillProvider getVerifySkillProvider() {
        return verifySkillProvider;
    }

    public void setVerifySkillProvider(VerifySkillProvider verifySkillProvider) {
        this.verifySkillProvider = verifySkillProvider;
    }

    public static class VerifySkillProvider {
        private String message;
        private int id;
        private String cacImagePath;
        private String providerId;

        // Getters and setters

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getCacImagePath() {
            return cacImagePath;
        }

        public void setCacImagePath(String cacImagePath) {
            this.cacImagePath = cacImagePath;
        }

        public String getProviderId() {
            return providerId;
        }

        public void setProviderId(String providerId) {
            this.providerId = providerId;
        }

        @Override
        public String toString() {
            return "VerifySkillProvider{" +
                    "message='" + message + '\'' +
                    ", id=" + id +
                    ", cacImagePath='" + cacImagePath + '\'' +
                    ", providerId='" + providerId + '\'' +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "VerifyFileResponse{" +
                "success=" + success +
                ", verifySkillProvider=" + verifySkillProvider +
                '}';
    }
}

