package io.jenkins.plugins.checks.github;

import edu.hm.hafner.util.FilteredLog;
import edu.umd.cs.findbugs.annotations.CheckForNull;
import hudson.model.Job;
import hudson.model.Run;
import jenkins.scm.api.SCMHead;
import jenkins.scm.api.SCMRevision;
import org.apache.commons.lang3.StringUtils;
import org.jenkinsci.plugins.github_branch_source.GitHubSCMSource;

import java.util.Optional;

/**
 * Provides a {@link GitHubChecksContext} for a Jenkins job that uses a supported {@link GitHubSCMSource}.
 */
class GitHubSCMSourceChecksContext extends GitHubChecksContext {
    @CheckForNull
    private final String sha;

    /**
     * Creates a {@link GitHubSCMSourceChecksContext} according to the run. All attributes are computed during this period.
     *
     * @param run
     *         a run of a GitHub Branch Source project
     * @param runURL
     *         the URL to the Jenkins run
     * @param scmFacade
     *         a facade for Jenkins SCM
     */
    GitHubSCMSourceChecksContext(final Run<?, ?> run, final String runURL, final SCMFacade scmFacade) {
        super(run.getParent(), runURL, scmFacade);
        sha = resolveHeadSha(run);
    }

    /**
     * Creates a {@link GitHubSCMSourceChecksContext} according to the job. All attributes are computed during this period.
     *
     * @param job
     *         a GitHub Branch Source project
     * @param jobURL
     *         the URL to the Jenkins job
     * @param scmFacade
     *         a facade for Jenkins SCM
     */
    GitHubSCMSourceChecksContext(final Job<?, ?> job, final String jobURL, final SCMFacade scmFacade) {
        super(job, jobURL, scmFacade);
        sha = resolveHeadSha(job);
    }

    @Override
    public String getHeadSha() {
        if (StringUtils.isBlank(sha)) {
            throw new IllegalStateException("No SHA found for job: " + getJob().getName());
        }

        return sha;
    }

    @Override
    public String getRepository() {
        GitHubSCMSource source = resolveSource();
        if (source == null) {
            throw new IllegalStateException("No GitHub SCM source found for job: " + getJob().getName());
        }
        else {
            return source.getRepoOwner() + "/" + source.getRepository();
        }
    }

    @Override
    public boolean isValid(final FilteredLog logger) {
        logger.logError("Trying to resolve checks parameters from GitHub SCM...");

        if (resolveSource() == null) {
            logger.logError("Job does not use GitHub SCM");

            return false;
        }

        if (!hasValidCredentials(logger)) {
            return false;
        }

        if (StringUtils.isBlank(sha)) {
            logger.logError("No HEAD SHA found for %s", getRepository());

            return false;
        }

        return true;
    }

    @Override
    @CheckForNull
    protected String getCredentialsId() {
        GitHubSCMSource source = resolveSource();
        if (source == null) {
            return null;
        }

        return source.getCredentialsId();
    }

    @CheckForNull
    private GitHubSCMSource resolveSource() {
        return getScmFacade().findGitHubSCMSource(getJob()).orElse(null);
    }

    @CheckForNull
    private String resolveHeadSha(final Run<?, ?> run) {
        GitHubSCMSource source = resolveSource();
        if (source != null) {
            Optional<SCMRevision> revision = getScmFacade().findRevision(source, run);
            if (revision.isPresent()) {
                return getScmFacade().findHash(revision.get()).orElse(null);
            }
        }

        return null;
    }

    @CheckForNull
    private String resolveHeadSha(final Job<?, ?> job) {
        GitHubSCMSource source = resolveSource();
        Optional<SCMHead> head = getScmFacade().findHead(job);
        if (source != null && head.isPresent()) {
            Optional<SCMRevision> revision = getScmFacade().findRevision(source, head.get());
            if (revision.isPresent()) {
                return getScmFacade().findHash(revision.get()).orElse(null);
            }
        }

        return null;
    }
}
