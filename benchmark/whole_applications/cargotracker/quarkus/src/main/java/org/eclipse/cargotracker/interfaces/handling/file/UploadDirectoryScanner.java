package org.eclipse.cargotracker.interfaces.handling.file;

import io.quarkus.scheduler.Scheduled;
import jakarta.batch.operations.JobOperator;
import jakarta.batch.runtime.BatchRuntime;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

/**
 * Periodically scans a certain directory for files and attempts to parse handling event
 * registrations from the contents by calling a batch job.
 *
 * <p>
 * Files that fail to parse are moved into a separate directory, successful files are deleted.
 */
@ApplicationScoped
@Transactional
public class UploadDirectoryScanner {

  @Scheduled(every = "2m") // In production, run every fifteen minutes
  public void processFiles() {
    JobOperator jobOperator = BatchRuntime.getJobOperator();
    jobOperator.start("EventFilesProcessorJob", null);
  }
}
