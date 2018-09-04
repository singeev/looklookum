package com.singeev.entity;

import java.text.DecimalFormat;
import java.util.Map;
import java.util.stream.Collectors;

public class DetectedFace {
    private String faceId;
    private FaceRectangle faceRectangle;
    private FaceAttributes faceAttributes;

    public DetectedFace() {
    }

    public String getFaceId() {
        return faceId;
    }

    public void setFaceId(String faceId) {
        this.faceId = faceId;
    }

    public FaceRectangle getFaceRectangle() {
        return faceRectangle;
    }

    public void setFaceRectangle(FaceRectangle faceRectangle) {
        this.faceRectangle = faceRectangle;
    }

    public FaceAttributes getFaceAttributes() {
        return faceAttributes;
    }

    public void setFaceAttributes(FaceAttributes faceAttributes) {
        this.faceAttributes = faceAttributes;
    }

    public class FaceRectangle {
        int top;
        int left;
        int width;
        int height;

        public FaceRectangle() {
        }

        public int getTop() {
            return top;
        }

        public void setTop(int top) {
            this.top = top;
        }

        public int getLeft() {
            return left;
        }

        public void setLeft(int left) {
            this.left = left;
        }

        public int getWidth() {
            return width;
        }

        public void setWidth(int width) {
            this.width = width;
        }

        public int getHeight() {
            return height;
        }

        public void setHeight(int height) {
            this.height = height;
        }
    }

    public class FaceAttributes {
        private double smile;
        private String gender;
        private double age;
//        private Emotion emotion;
        private Map<String, Double> emotion;

        public FaceAttributes() {
        }

        public void setEmotion(Map<String, Double> emotion) {
            this.emotion = emotion;
        }

        public Map<String, Double> getEmotion() {
            return emotion;
        }

        public double getSmile() {
            return smile;
        }

        public void setSmile(double smile) {
            this.smile = smile;
        }

        public String getGender() {
            return gender;
        }

        public void setGender(String gender) {
            this.gender = gender;
        }

        public double getAge() {
            return age;
        }

        public void setAge(double age) {
            this.age = age;
        }

//        public Emotion getEmotion() {
//            return emotion;
//        }
//
//        public void setEmotion(Emotion emotion) {
//            this.emotion = emotion;
//        }

        public class Emotion {
            private double anger;
            private double contempt;
            private double disgust;
            private double fear;
            private double happiness;
            private double neutral;
            private double sadness;
            private double surprise;

            public Emotion() {
            }

            public double getAnger() {
                return anger;
            }

            public void setAnger(double anger) {
                this.anger = anger;
            }

            public double getContempt() {
                return contempt;
            }

            public void setContempt(double contempt) {
                this.contempt = contempt;
            }

            public double getDisgust() {
                return disgust;
            }

            public void setDisgust(double disgust) {
                this.disgust = disgust;
            }

            public double getFear() {
                return fear;
            }

            public void setFear(double fear) {
                this.fear = fear;
            }

            public double getHappiness() {
                return happiness;
            }

            public void setHappiness(double happiness) {
                this.happiness = happiness;
            }

            public double getNeutral() {
                return neutral;
            }

            public void setNeutral(double neutral) {
                this.neutral = neutral;
            }

            public double getSadness() {
                return sadness;
            }

            public void setSadness(double sadness) {
                this.sadness = sadness;
            }

            public double getSurprise() {
                return surprise;
            }

            public void setSurprise(double surprise) {
                this.surprise = surprise;
            }
        }
    }

    @Override
    public String toString() {
        String emotions = this.faceAttributes.getEmotion().entrySet()
                .stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .map(entry -> " - " + entry.getKey() + ": " + new DecimalFormat("#.##%").format(entry.getValue()))
                .collect(Collectors.joining("\n"));
        return "Gender: " + this.faceAttributes.getGender() + "\n" +
                "Age: " + this.faceAttributes.getAge() + "\n" +
                "Emotions: \n" + emotions;
    }
}
