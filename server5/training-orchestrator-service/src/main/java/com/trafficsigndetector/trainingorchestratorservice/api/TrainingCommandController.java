package com.trafficsigndetector.trainingorchestratorservice.api;

import com.trafficsigndetector.trainingorchestratorservice.model.ErrorResponse;
import com.trafficsigndetector.trainingorchestratorservice.model.QueueEnqueueResponse;
import com.trafficsigndetector.trainingorchestratorservice.model.StartTrainingResponse;
import com.trafficsigndetector.trainingorchestratorservice.model.TimelineResponse;
import com.trafficsigndetector.trainingorchestratorservice.messaging.TrainingJobMessage;
import com.trafficsigndetector.trainingorchestratorservice.messaging.TrainingJobPublisher;
import com.trafficsigndetector.trainingorchestratorservice.messaging.TrainingStatusMessage;
import com.trafficsigndetector.trainingorchestratorservice.messaging.TrainingStatusTracker;
import com.trafficsigndetector.trainingorchestratorservice.service.QueueCapacityExceededException;
import com.trafficsigndetector.trainingorchestratorservice.service.TrainingSessionService;
import com.trafficsigndetector.sharedmodel.ThongTinHL;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.Instant;

@RestController
@RequestMapping("/api")
public class TrainingCommandController {

    private final TrainingJobPublisher trainingJobPublisher;
    private final TrainingStatusTracker trainingStatusTracker;
    private final TrainingSessionService trainingSessionService;

    public TrainingCommandController(
            TrainingJobPublisher trainingJobPublisher,
            TrainingStatusTracker trainingStatusTracker,
            TrainingSessionService trainingSessionService
    ) {
        this.trainingJobPublisher = trainingJobPublisher;
        this.trainingStatusTracker = trainingStatusTracker;
        this.trainingSessionService = trainingSessionService;
    }

    @PostMapping(value = "/huan-luyen", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ThongTinHL> createTraining(@RequestBody ThongTinHL payload) {
        return ResponseEntity.ok(trainingSessionService.createSession(payload));
    }

    @PostMapping(value = "/huan-luyen/{thongTinHLId}/bat-dau", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<StartTrainingResponse> startTraining(@PathVariable int thongTinHLId) {
        trainingSessionService.startTraining(thongTinHLId);
        return ResponseEntity.accepted().body(new StartTrainingResponse(thongTinHLId, "QUEUED"));
    }

    @GetMapping(value = "/huan-luyen/{thongTinHLId}/ket-qua", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ThongTinHL> getTrainingResult(@PathVariable int thongTinHLId) {
        return ResponseEntity.ok(trainingSessionService.getResult(thongTinHLId));
    }

    @PostMapping(value = "/huan-luyen/{thongTinHLId}/luu-phien-ban", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Integer> saveModelVersion(@PathVariable int thongTinHLId) {
        return ResponseEntity.ok(trainingSessionService.saveVersion(thongTinHLId));
    }

    @PostMapping("/trainings/{trackingId}/enqueue")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public QueueEnqueueResponse enqueueTraining(
            @PathVariable String trackingId,
            @RequestParam(defaultValue = "traffic-sign-default") String modelCode
    ) {
        TrainingJobMessage message = new TrainingJobMessage(trackingId, modelCode, Instant.now());
        trainingJobPublisher.publish(message);

        return new QueueEnqueueResponse(trackingId, modelCode, "QUEUED");
    }

    @GetMapping(value = "/trainings/{trackingId}/status", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TrainingStatusMessage> getLatestStatus(@PathVariable String trackingId) {
        return trainingSessionService.getLatestStatus(trackingId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/trainings/{trackingId}/events")
    public TimelineResponse getStatusTimeline(@PathVariable String trackingId) {
        return new TimelineResponse(trackingId, trainingSessionService.getTimeline(trackingId));
    }

    @GetMapping(value = "/trainings/{trackingId}/stream", produces = "text/event-stream")
    public SseEmitter streamStatus(@PathVariable String trackingId) {
        SseEmitter emitter = trainingStatusTracker.subscribe(trackingId);
        trainingSessionService.getLatestStatus(trackingId).ifPresent(latest -> {
            try {
                emitter.send(SseEmitter.event().name("training-status").data(latest));
            } catch (Exception ex) {
                emitter.completeWithError(ex);
            }
        });
        return emitter;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatusCode.valueOf(404))
                .contentType(MediaType.APPLICATION_JSON)
                .body(new ErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalState(IllegalStateException ex) {
        return ResponseEntity.status(HttpStatusCode.valueOf(409))
                .contentType(MediaType.APPLICATION_JSON)
                .body(new ErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(QueueCapacityExceededException.class)
    public ResponseEntity<ErrorResponse> handleQueueCapacityExceeded(QueueCapacityExceededException ex) {
        return ResponseEntity.status(HttpStatusCode.valueOf(429))
                .contentType(MediaType.APPLICATION_JSON)
                .body(new ErrorResponse(ex.getMessage()));
    }
}

