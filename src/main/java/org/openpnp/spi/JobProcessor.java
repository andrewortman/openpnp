package org.openpnp.spi;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.openpnp.model.Job;
import org.openpnp.util.UiUtils.Thrunnable;

public interface JobProcessor extends PropertySheetHolder, WizardConfigurable {
    public interface TextStatusListener {
        public void textStatus(String text);
    }

    public void initialize(Job job) throws Exception;

    public boolean next() throws JobProcessorException;

    boolean isSteppingToNextMotion();

    public void abort() throws JobProcessorException;    

    public void addTextStatusListener(TextStatusListener listener);

    public void removeTextStatusListener(TextStatusListener listener);
    
    public class JobProcessorException extends Exception {
        private static final long serialVersionUID = 1L;

        private final Object source;
        private boolean interrupting = false;

        private static String getThrowableMessage(Throwable throwable) {
            if (throwable.getMessage() != null) {
                return throwable.getMessage();
            }
            // If a message is missing, use the stack trace as the message
            // (same behavior as MessageBoxes.errorBox() when given an exception directly).
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            throwable.printStackTrace(pw);
            return sw.toString();
        }

        public JobProcessorException(Object source, Throwable throwable) {
            super(getThrowableMessage(throwable), throwable);
            this.source = source;
            if (throwable instanceof JobProcessorException) {
                this.interrupting = ((JobProcessorException) throwable).isInterrupting();
            }
        }

        public JobProcessorException(Object source, String message) {
            super(message);
            this.source = source;
        }

        public JobProcessorException(Object source, String message, boolean interrupting) {
            super(message);
            this.source = source;
            this.interrupting = interrupting;
        }

        public Object getSource() {
            return source;
        }

        public boolean isInterrupting() {
            return interrupting;
        }
    }
    
    // this extended exception class allows to specify a task to be executed once the user has agreed
    public class JobProcessorExceptionWithContinuation extends JobProcessorException {
        private static final long serialVersionUID = 1L;

        final Thrunnable thrunnable;
        
        public JobProcessorExceptionWithContinuation(JobProcessorException e, Thrunnable thrunnable) {
            super(e.getSource(), e);
            this.thrunnable = thrunnable;
        }
        
        public JobProcessorExceptionWithContinuation(Object source, String message, boolean interrupting, Thrunnable thrunnable) {
            super(source, message, interrupting);
            this.thrunnable = thrunnable;
        }

        public Thrunnable getThrunnable() {
            return thrunnable;
        }
    }
    
    public class ManualUnloadException extends JobProcessorException {
        private static final long serialVersionUID = 1L;

        public ManualUnloadException(Object source, String message, boolean interrupting) {
            super(source, message, interrupting);
        }
    }

}
