package com.example.minhtuyen.mushroomcontroller;

public class Topic1 implements ITopic {
    @Override
    public String autoTopic() {
        return "1MyMushroomHousetkb2e32/home/auto";
    }

    @Override
    public String manualTopic() {
        return "2MyMushroomHousetkb2e32/home/manual";
    }

    @Override
    public String requestTopic() {
        return "3MyMushroomHousetkb2e32/home/request";
    }

    @Override
    public String statusTopic() {
        return "MyMushroomHousetkb2e32/home/status";
    }

    @Override
    public String updateTopic() {
        return "MyMushroomHousetkb2e32/home/info";
    }

    @Override
    public String listFileUpdateTopic() {
        return "MyMushroomHousetkb2e32/home/update/listFile";
    }

    @Override
    public String fileContentUpdateTopic() {
        return "MyMushroomHousetkb2e32/home/update/content";
    }

    @Override
    public String listFireRequestTopic() {
        return "4MyMushroomHousetkb2e32/home/listFile";
    }

    @Override
    public String fileContentRequestTopic() {
        return "5MyMushroomHousetkb2e32/home/content";
    }
}
