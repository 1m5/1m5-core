package io.onemfive.core;

import io.onemfive.data.Envelope;

public interface Operation {
    void execute(Envelope envelope);
}
