package ru.ifmo.ailab.daafse.alertservice;

public interface StreamReaderService {

    public void startReadStream(final StreamURI uri);

    public void stopReadStream(final StreamURI uri);

}
