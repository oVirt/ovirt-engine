package org.ovirt.engine.core.common.flow;

import static org.ovirt.engine.core.common.flow.HandlerOutcome.EXCEPTION;
import static org.ovirt.engine.core.common.flow.HandlerOutcome.FAILURE;
import static org.ovirt.engine.core.common.flow.HandlerOutcome.NEUTRAL;
import static org.ovirt.engine.core.common.flow.HandlerOutcome.SUCCESS;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractHandler<C extends Context> implements Handler<C> {
    private static final Logger logger = LoggerFactory.getLogger(AbstractHandler.class);

    private Handler<C> onSuccess;
    private Handler<C> onNeutral;
    private Handler<C> onFailure;
    private Handler<C> onException;
    protected String name;

    protected AbstractHandler() {
        name = getClass().getSimpleName();
    }

    @Override
    public final void process(C ctx) {
        if (logger.isDebugEnabled()) {
            logger.debug("On flow {} processing handler {}", ctx.getId(), getName());
        }
        HandlerOutcome outcome = tryHandle(ctx);
        switch (outcome) {
        case SUCCESS:
            processCase(onSuccess, ctx, SUCCESS);
            break;
        case NEUTRAL:
            processCase(onNeutral, ctx, NEUTRAL);
            break;
        case FAILURE:
            processCase(onFailure, ctx, FAILURE);
            break;
        case EXCEPTION:
            logger.error("Exception in handler {}", getClass().getCanonicalName(), ctx.getException());
            processCase(onException, ctx, EXCEPTION);
            break;
        default:
            logger.error("On flow {} handler {} terminated the flow with {}", ctx.getId(), getName(), outcome);
            break;
        }
    }

    private void processCase(Handler<C> handler, C ctx, HandlerOutcome outcome) {
        if (logger.isDebugEnabled()) {
            logger.debug("On flow {} handler {} returned {}", ctx.getId(), getName(), outcome.name());
        }
        if (handler != null) {
            handler.process(ctx);
        }
    }

    private HandlerOutcome tryHandle(C ctx) {
        try {
            if (!validateContext(ctx)) {
                logger.error("On flow {} handler validation failed: failure message {} handler {}", ctx.getId(), getName());
                return HandlerOutcome.FAILURE;
            }
            return handle(ctx);
        } catch (Exception e) {
            ctx.setException(e);
            ctx.trace(HandlerOutcome.EXCEPTION, ctx, e, e.getStackTrace());
            return HandlerOutcome.EXCEPTION;
        }
    }

    /**
     * Optionally validate the context.
     * Override this method for specific validation in the handlers.
     * @return false if the flow should fail on validation
     */
    protected boolean validateContext(C ctx) {
        return true;
    }

    public abstract HandlerOutcome handle(C ctx)  throws Exception;

    @Override
    public Handler<C> setOnSuccess(Handler<C> successHandler) {
        this.onSuccess = successHandler;
        return this;
    }

    @Override
    public Handler<C> setOnNeutral(Handler<C> neutralHandler) {
        this.onNeutral = neutralHandler;
        return this;
    }

    @Override
    public Handler<C> setOnFailure(Handler<C> failureHandler) {
        this.onFailure = failureHandler;
        return this;
    }

    @Override
    public Handler<C> setOnException(Handler<C> exceptionHandler) {
        this.onException = exceptionHandler;
        return this;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }
    @Override
    public void print(StringBuilder sb) {
        printBranch(sb, onSuccess, "success");
        printBranch(sb, onNeutral, "neutral");
        printBranch(sb, onFailure, "failure");
        printBranch(sb, onException, "exception");
    }

    private void printBranch(StringBuilder sb, Handler<C> handler, String label) {
        if (handler != null) {
            printCurrentHandler(sb, handler.getName(), label);
            handler.print(sb);
        }
    }

    private void printCurrentHandler(StringBuilder sb, String name, String label) {
        String color = "success".equals(label) ? "0.408 0.498 1.000" : "failure".equals(label) ? "0.002 0.999 0.999" : "exception".equals(label) ? "0.002 0.999 0.999" : "0.650 0.700 0.700";
        String line = "\"" + getName() + "\" -> \"" + name + "\" [ label=" + label + ", color=\"" + color + "\" ];\n";
        if (sb.indexOf(line) < 0) {
            sb.append(line);
        }
    }
}
