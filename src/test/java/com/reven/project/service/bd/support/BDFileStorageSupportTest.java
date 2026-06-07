// 저장 경로 해석의 rootPath 경로 탈출 가드를 검증하는 테스트
package com.reven.project.service.bd.support;

import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.assertj.core.api.Assertions.assertThat;

class BDFileStorageSupportTest {

    @TempDir
    Path tempDir;

    @Test
    void resolveStoredFilePathReturnsResolvedPathInsideRoot() {
        BDFileStorageSupport storage = new BDFileStorageSupport(tempDir.toString());

        Path resolved = storage.resolveStoredFilePath("2026/05/31", "file.pdf");

        assertThat(resolved).isEqualTo(tempDir.resolve("2026/05/31/file.pdf").toAbsolutePath().normalize());
    }

    @Test
    void resolveStoredFilePathReturnsNullWhenPathEscapesRoot() {
        BDFileStorageSupport storage = new BDFileStorageSupport(tempDir.toString());

        assertThat(storage.resolveStoredFilePath("../../etc", "passwd")).isNull();
    }
}
