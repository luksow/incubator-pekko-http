Release Akka HTTP $VERSION$

<!--
Release Train Issue Template for Akka HTTP

(Liberally copied and adopted from Scala itself https://github.com/scala/scala-dev/blob/b11cd2e4a4431de7867db6b39362bea8fa6650e7/notes/releases/template.md)

For every Akka HTTP release, make a copy of this file named after the release, and expand the variables.
Ideally replacing variables could become a script you can run on your local machine.

Variables to be expanded in this template:
- VERSION=???

Key links:
  - akka/akka-http milestone: https://github.com/apache/incubator-pekko-http/milestone/?
-->

### ~ 1 week before the release
- [ ] Check that open PRs and issues assigned to the milestone are reasonable
- [ ] Triage tickets that should be ready for this release, add "pick next" label and release milestone
- [ ] Triage open PRs and apply "pick next" label and maybe add to release milestone. Some PRs might be explicitly scheduled for this release, others might be ready enough to bring them over the finish line 
Wind down PR queue. There has to be enough time after the last (non-trivial) PR is merged and the next phase. The core of the eco-system needs time to prepare for the final!
- [ ] Decide on planned release date
- [ ] Notify depending projects (notably Play + cinnamon) about upcoming release

### 1 day before the release
- [ ] Make sure all important / big PRs have been merged by now
- [ ] Check that latest snapshot release still works with depending projects (notably Play + cinnamon)
- [ ] Communicate that a release is about to be released in [Gitter Akka Dev Channel](https://gitter.im/akka/dev), so that no new Pull Requests are merged

### Preparing release notes in the documentation / announcement

- [ ] Create a PR to add a release notes entry in docs/src/main/paradox/release-notes/. As a helper run `scripts/commits-for-release-notes.sh <last-version-tag>` which will output a list of commits grouped by submodule, and the closed issues for this milestone
- [ ] Create a draft PR on https://github.com/akka/akka.io with a news item (using the milestones, release notes and `scripts/authors.scala previousVersion origin/main`) and updating `_config.yml`
- [ ] Release notes PR has been merged
- [ ] Create a new milestone for the next version at https://github.com/apache/incubator-pekko-http/milestones
- [ ] Move all unclosed issues to the newly created milestone (or remove milestone) and close the version you're releasing

### Cutting the release

- [ ] Wait until [main build finished](https://github.com/apache/incubator-pekko-http/actions/workflows/publish.yml?query=event%3Apush) after merging the release notes (otherwise, the main
      build might pick up the tag and start publishing the release uhoh)
- [ ] Create a tag for the release (e.g. `git tag -s -a v$VERSION$ -m "Release $VERSION$"`) and push it.
- [ ] Check that the Github Actions [release build](https://github.com/apache/incubator-pekko-http/actions/workflows/publish.yml?query=event%3Apush) executes successfully
- [ ] Notify Telemetry / Play team to check against staged artifacts
- [ ] Run a test against the staging repository to make sure the release went well, for example by using https://github.com/apache/incubator-pekko-http-quickstart-scala.g8 and adding the sonatype staging repo with `resolvers += "Staging Repo" at "https://oss.sonatype.org/content/repositories/staging"`
- [ ] Release the staging repository to Maven Central.

### Check availability
- [ ] Check the release on maven central: https://repo1.maven.org/maven2/org/apache/pekko/akka-http-core_2.13/$VERSION$/

### When everything is on maven central

  - [ ] `ssh akkarepo@gustav.akka.io`
    - [ ] update the `10.2` and `current` links on `repo.akka.io` to point to the latest version with (**replace the minor appropriately**)
         ```
         ln -nsf $VERSION$ www/docs/akka-http/10.2
         ln -nsf $VERSION$ www/api/akka-http/10.2
         ln -nsf $VERSION$ www/japi/akka-http/10.2
         ln -nsf $VERSION$ www/docs/akka-http/current
         ln -nsf $VERSION$ www/api/akka-http/current
         ln -nsf $VERSION$ www/japi/akka-http/current
         ```
    - [ ] check changes and commit the new version to the local git repository
         ```
         cd ~/www
         git add docs/akka-http/ api/akka-http/ japi/akka-http/
         git commit -m "Akka HTTP $VERSION$"
         ```
    - [ ] push changes to the [remote git repository](https://github.com/akka/doc.akka.io)
         ```
         cd ~/www
         git push origin master
         ```
  - [ ] Merge draft news item at https://github.com/akka/akka.io/pulls
  - [ ] Wait until the release page is published
  - [ ] Create the GitHub [release](https://github.com/apache/incubator-pekko-http/releases/tag/v$VERSION$) with the tag, title and release description linking to announcement, release notes and milestone.

### Announcements
- [ ] Send a release notification to https://discuss.akka.io
- [ ] Tweet using the akka account (or ask someone to) about the new release
- [ ] Announce on Gitter at https://gitter.im/akka/akka (e.g. `@/all we are happy to announce the latest Akka HTTP release $VERSION$, see https://akka.io/blog/news/XYZ for more information`)

### Afterwards
- [ ] Add the released version to `project/MiMa.scala` to the `mimaPreviousArtifacts` key *of all current compatible branches*.
- [ ] Forward port release notes from old releases to main
- [ ] Update Akka HTTP reference in [lightbend-platform-docs](https://github.com/lightbend/lightbend-platform-docs/blob/master/docs/modules/getting-help/examples/build.sbt#L149)
- [ ] Update Akka HTTP reference in [akka-platform-dependencies](https://github.com/lightbend/akka-platform-dependencies/blob/main/project/Dependencies.scala)
- [ ] Update Akka HTTP references everywhere in [akka-platform-guide](https://github.com/akka/akka-platform-guide) using the [script]([https://github.com/akka/akka-platform-guide/blob/3079fd3f8cdbef03d352ae88fb4a8d5a9b1f1be6/scripts/update-akka-http-version.sh) by running
  `scripts/update-akka-http-version.sh $VERSION$`
- Close this issue
