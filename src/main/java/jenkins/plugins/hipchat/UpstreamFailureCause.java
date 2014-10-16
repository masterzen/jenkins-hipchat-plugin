package jenkins.plugins.hipchat;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import jenkins.model.Jenkins;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.lang.StringUtils;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;

import hudson.Util;
import hudson.model.Result;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Cause;
import hudson.model.User;

public class UpstreamFailureCause
{
  private final AbstractBuild build;

  public UpstreamFailureCause(AbstractBuild build)
  {
    this.build = build;
  }

  public Set<User> getUpstream()
  {
    if (build.getResult() == Result.SUCCESS || build.getCause(Cause.UpstreamCause.class) == null)
      return Collections.emptySet();

    ArrayList<Cause.UpstreamCause> upstreamCauses = getUpstreamCauses(build);
    ArrayList<AbstractProject> upstreamProjects = getUpstreamProjects(upstreamCauses);
    Set<User> culprits = getCulprits(upstreamCauses);

    if (upstreamProjects.isEmpty()) {
      return Collections.emptySet();
    }

    if (culprits.isEmpty()) {
      Collections.emptySet();
    }

    return culprits;
  }

  private Set<InternetAddress> buildCulpritList(Set<User> culprits) throws UnsupportedEncodingException
  {
    Set<InternetAddress> r = new HashSet<InternetAddress>();
    for (User a : culprits) {
      String addresses = Util.fixEmpty(a.getProperty(hudson.tasks.Mailer.UserProperty.class).getAddress());

      if (addresses != null) {
        try {
          r.add(hudson.tasks.Mailer.StringToAddress(addresses, "UTF-8"));
        } catch (AddressException e) {
        }
      }
    }
    return r;
  }

  private Set<User> getCulprits(ArrayList<Cause.UpstreamCause> upstreamCauses)
  {

    Set<User> culprits = new HashSet<User>();

    for (Cause.UpstreamCause cause : upstreamCauses) {
      AbstractBuild build = ((AbstractProject) Jenkins.getInstance().getItem(cause.getUpstreamProject())).getBuildByNumber(cause.getUpstreamBuild());
      culprits.addAll(build.getCulprits());
    }

    return culprits;
  }

  private Collection<String> getProjectNames(Collection<AbstractProject> projects)
  {
    Collection<String> namesCollection = Collections2.transform(projects, new Function<AbstractProject, String>() {
      public String apply(AbstractProject from)
      {
        return from.getName();
      }
    });

    return namesCollection;
  }

  private ArrayList<AbstractProject> getUpstreamProjects(ArrayList<Cause.UpstreamCause> upstreamCauses)
  {

    ArrayList<AbstractProject> projects = new ArrayList<AbstractProject>();

    for (Cause.UpstreamCause cause : upstreamCauses) {
      String project = cause.getUpstreamProject();
      projects.add((AbstractProject) Jenkins.getInstance().getItem(project));
    }

    return projects;
  }

  private ArrayList<Cause.UpstreamCause> getUpstreamCauses(AbstractBuild<?, ?> build)
  {
    Cause.UpstreamCause buildCause = build.getCause(Cause.UpstreamCause.class);

    ArrayList<Cause.UpstreamCause> causes = new ArrayList<Cause.UpstreamCause>();

    causes.add(buildCause);

    getUpstreamCauses(buildCause.getUpstreamCauses(), causes);

    return causes;
  }

  private void getUpstreamCauses(List<Cause> upstreamCauses, final ArrayList<Cause.UpstreamCause> causes)
  {

    causes.addAll(CollectionUtils.select(upstreamCauses, new Predicate() {
      public boolean evaluate(Object o)
      {
        if (o instanceof Cause.UpstreamCause) {
          Cause.UpstreamCause cause = (Cause.UpstreamCause) o;
          getUpstreamCauses(cause.getUpstreamCauses(), causes);
        }

        return o instanceof Cause.UpstreamCause;
      }
    }));
  }

}
