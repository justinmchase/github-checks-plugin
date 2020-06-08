package io.jenkins.plugins.github.checks.api;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;

import io.jenkins.plugins.github.checks.ChecksConclusion;
import io.jenkins.plugins.github.checks.ChecksStatus;

public class ChecksDetails {
    private final String name;
    private final ChecksStatus status;
    private final String detailsURL;
    private final ChecksConclusion conclusion;
    private final List<Output> outputs;
    private final List<Action> actions;

    private ChecksDetails(final String name, final ChecksStatus status, final String detailsURL,
            final ChecksConclusion conclusion, final List<Output> outputs, final List<Action> actions) {
        this.name = name;
        this.status = status;
        this.detailsURL = detailsURL;
        this.conclusion = conclusion;
        this.outputs = outputs;
        this.actions = actions;
    }

    /**
     * Returns the name of a check.
     *
     * @return the unique name of a check
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the status of a check.
     *
     * @return {@link ChecksStatus}, one of {@code QUEUED}, {@code IN_PROGRESS}, {@code COMPLETED}
     */
    public ChecksStatus getStatus() {
        return status;
    }

    /**
     * Returns the url of a site with full details of the check.
     *
     * @return the string representing the url of a site
     */
    public String getDetailsURL() {
        return detailsURL;
    }

    /**
     * Returns the conclusion of a check.
     *
     * @return {@link ChecksConclusion}, one of {@code SUCCESS}, {@code FAILURE}, {@code NEUTRAL}, {@code CANCELLED},
     *         {@code SKIPPED}, {@code TIME_OUT}, or {@code ACTION_REQUIRED} when {@link ChecksDetails#getStatus()}
     *         returns {@code COMPLETED}, otherwise an empty string
     */
    public ChecksConclusion getConclusion() {
        return conclusion;
    }

    /**
     * Returns the {@link Output}s of a check
     *
     * @return An immutable list of {@link Output}s of a check
     */
    public List<Output> getOutputs() {
        return outputs;
    }

    /**
     * Returns the {@link Action}s of a check
     *
     * @return An immutable list of {@link Action}s of a check
     */
    public List<Action> getActions() {
        return actions;
    }

    public static class ChecksDetailsBuilder {
        private final String name;
        private final ChecksStatus status;
        private String detailsURL;
        private ChecksConclusion conclusion;
        private List<Output> outputs = Collections.emptyList();
        private List<Action> actions = Collections.emptyList();

        public ChecksDetailsBuilder(final String name, final ChecksStatus status)
                throws IllegalArgumentException{
            Objects.requireNonNull(status);
            if (StringUtils.isBlank(name)) {
                throw new IllegalArgumentException("check name should not be blank");
            }

            this.name = name;
            this.status = status;
        }

        public ChecksDetailsBuilder withDetailsURL(final String detailsURL) {
            Objects.requireNonNull(detailsURL);
            this.detailsURL = detailsURL;
            return this;
        }

        public ChecksDetailsBuilder withConclusion(final ChecksConclusion conclusion) {
            Objects.requireNonNull(conclusion);

            if (status != ChecksStatus.COMPLETED) {
                throw new IllegalArgumentException("status must be completed when setting conclusion");
            }
            this.conclusion = conclusion;
            return this;
        }

        public ChecksDetailsBuilder withOutputs(final List<Output> outputs) {
            Objects.requireNonNull(outputs);
            this.outputs = Collections.unmodifiableList(outputs);
            return this;
        }

        public ChecksDetailsBuilder withActions(List<Action> actions) {
            Objects.requireNonNull(actions);
            this.actions = Collections.unmodifiableList(actions);
            return this;
        }

        /**
         * Actually build the {@code ChecksDetail}.
         *
         * @return the built {@code ChecksDetail}
         * @throws IllegalArgumentException if {@code conclusion} is null when {@code status} is {@code completed}
         */
        public ChecksDetails build() throws IllegalArgumentException {
            if (conclusion == null && status == ChecksStatus.COMPLETED) {
                throw new IllegalArgumentException("conclusion must be set when status is completed");
            }

            return new ChecksDetails(name, status, detailsURL, conclusion, outputs, actions);
        }
    }
}

