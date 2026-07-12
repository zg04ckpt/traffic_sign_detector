package com.trafficsigndetector.trainingorchestratorservice.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

@Service
public class TrainingFileStorageService {

    private final Path workingRoot;
    private final Path workerTempModelDir;
    private final Path aimodelModelStoreDir;
    private final String aimodelModelPublicPrefix;

    public TrainingFileStorageService(
            @Value("${app.training.artifact.temp-model-dir:../ai-training-service/runtime/training/outputs}") String workerTempModelDir,
            @Value("${app.training.artifact.aimodel-model-dir:../aimodel-service/models}") String aimodelModelStoreDir,
            @Value("${app.training.artifact.aimodel-public-prefix:/models}") String aimodelModelPublicPrefix
    ) {
        this.workingRoot = Path.of("").toAbsolutePath().normalize();
        this.workerTempModelDir = resolvePath(workerTempModelDir);
        this.aimodelModelStoreDir = resolvePath(aimodelModelStoreDir);
        this.aimodelModelPublicPrefix = aimodelModelPublicPrefix;
    }

    public Path resolveTempModelPath(String artifactPath) {
        String normalized = artifactPath == null ? "" : artifactPath.trim();
        if (normalized.isEmpty()) {
            throw new IllegalStateException("Training result has no temporary model artifact path");
        }

        Path absolute = toAbsolutePath(normalized);
        if (absolute != null && Files.exists(absolute)) {
            return absolute;
        }

        String slashNormalized = normalized.replace('\\', '/');
        String fileName = extractFileName(slashNormalized);

        Path candidateFromKnownDirs = resolveTempArtifactFromKnownDirs(fileName);
        if (candidateFromKnownDirs != null) {
            return candidateFromKnownDirs;
        }

        if (slashNormalized.startsWith("/runtime/training/outputs/")
                || slashNormalized.startsWith("runtime/training/outputs/")
                || slashNormalized.startsWith("/models/outputs/")
                || slashNormalized.startsWith("models/outputs/")
                || !slashNormalized.contains("/")) {
            Path candidate = workerTempModelDir.resolve(fileName).normalize();
            if (Files.exists(candidate)) {
                return candidate;
            }
        }

        String localRelative = slashNormalized.startsWith("/") ? slashNormalized.substring(1) : slashNormalized;

        Path relative = workingRoot.resolve(localRelative).normalize();
        if (Files.exists(relative)) {
            return relative;
        }

        Path moduleRelative = workingRoot.resolve("training-orchestrator-service").resolve(localRelative).normalize();
        if (Files.exists(moduleRelative)) {
            return moduleRelative;
        }

        Path server5ModuleRelative = workingRoot.resolve("server5")
                .resolve("training-orchestrator-service")
                .resolve(localRelative)
                .normalize();
        if (Files.exists(server5ModuleRelative)) {
            return server5ModuleRelative;
        }

        throw new IllegalStateException("Temporary model artifact not found: " + artifactPath);
    }

    private Path resolveTempArtifactFromKnownDirs(String fileName) {
        List<Path> candidateDirs = List.of(
                workerTempModelDir,
                workingRoot.resolve("runtime").resolve("training").resolve("outputs"),
                workingRoot.resolve("server5").resolve("runtime").resolve("training").resolve("outputs"),
                workingRoot.resolve("ai-training-service").resolve("runtime").resolve("training").resolve("outputs"),
                workingRoot.resolve("server5").resolve("ai-training-service").resolve("runtime").resolve("training").resolve("outputs"),
                workingRoot.resolve("ai-training-service").resolve("models").resolve("outputs"),
                workingRoot.resolve("server5").resolve("ai-training-service").resolve("models").resolve("outputs")
        );

        for (Path dir : candidateDirs) {
            Path candidate = dir.resolve(fileName).normalize();
            if (Files.exists(candidate)) {
                return candidate;
            }
        }
        return null;
    }

    public String buildTargetFileName(String versionName, String sourceFileName) {
        String extension = ".pt";
        int dotIndex = sourceFileName.lastIndexOf('.');
        if (dotIndex >= 0 && dotIndex < sourceFileName.length() - 1) {
            extension = sourceFileName.substring(dotIndex);
        }

        String safeBase = versionName == null ? "version" : versionName.replaceAll("[^a-zA-Z0-9._-]", "_");
        if (safeBase.isBlank()) {
            safeBase = "version";
        }
        if (safeBase.length() > 80) {
            safeBase = safeBase.substring(0, 80);
        }

        return safeBase + extension;
    }

    public String normalizeAimodelPublicPath(int moHinhId, String fileName) {
        String prefix = aimodelModelPublicPrefix.endsWith("/")
                ? aimodelModelPublicPrefix.substring(0, aimodelModelPublicPrefix.length() - 1)
                : aimodelModelPublicPrefix;
        return prefix + "/mo-hinh-" + moHinhId + "/" + fileName;
    }

    public Path copyToAimodelStore(Path source, int moHinhId, String targetFileName) {
        Path aimodelTargetPath = aimodelModelStoreDir.resolve("mo-hinh-" + moHinhId).resolve(targetFileName).normalize();
        try {
            Files.createDirectories(aimodelTargetPath.getParent());
            Files.copy(source, aimodelTargetPath, StandardCopyOption.REPLACE_EXISTING);
            return aimodelTargetPath;
        } catch (IOException ex) {
            throw new IllegalStateException("Could not copy temporary model artifact to aimodel-service storage", ex);
        }
    }

    public void deleteTempArtifact(Path tempModelPath) {
        try {
            Files.deleteIfExists(tempModelPath);
        } catch (IOException ex) {
            throw new IllegalStateException("Saved version but could not delete temporary model artifact: " + tempModelPath, ex);
        }
    }

    public void deleteIfExistsQuietly(Path path) {
        try {
            Files.deleteIfExists(path);
        } catch (IOException ignored) {
            // Best-effort cleanup only.
        }
    }

    private Path resolvePath(String raw) {
        Path path = Path.of(raw);
        if (path.isAbsolute()) {
            return path.normalize();
        }

        Path direct = workingRoot.resolve(path).normalize();
        Path moduleRelative = workingRoot.resolve("training-orchestrator-service").resolve(path).normalize();
        Path server5Direct = workingRoot.resolve("server5").resolve(path).normalize();
        Path server5ModuleRelative = workingRoot.resolve("server5")
                .resolve("training-orchestrator-service")
                .resolve(path)
                .normalize();

        if (Files.exists(direct)) {
            return direct;
        }
        if (Files.exists(moduleRelative)) {
            return moduleRelative;
        }
        if (Files.exists(server5Direct)) {
            return server5Direct;
        }
        if (Files.exists(server5ModuleRelative)) {
            return server5ModuleRelative;
        }
        return direct;
    }

    private Path toAbsolutePath(String raw) {
        try {
            Path path = Path.of(raw);
            return path.isAbsolute() ? path.normalize() : null;
        } catch (Exception ex) {
            return null;
        }
    }

    private String extractFileName(String input) {
        int slash = input.lastIndexOf('/');
        return slash >= 0 ? input.substring(slash + 1) : input;
    }
}
