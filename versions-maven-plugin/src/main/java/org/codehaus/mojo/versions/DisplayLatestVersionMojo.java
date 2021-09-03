package org.codehaus.mojo.versions;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import javax.inject.Inject;

import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.repository.RepositorySystem;
import org.apache.maven.wagon.Wagon;
import org.codehaus.mojo.versions.api.VersionRetrievalException;
import org.codehaus.mojo.versions.api.recording.ChangeRecorder;
import org.codehaus.mojo.versions.rewriting.ModifiedPomXMLEventReader;

import static java.lang.String.format;

/**
 * Displays the latest version of an artefact
 *
 * @author Alexander GÄngel
 * @since 2.9
 */
@Mojo(name = "display-latest-version", requiresProject = false, requiresDirectInvocation = true, threadSafe = true)
public class DisplayLatestVersionMojo extends AbstractVersionsDisplayMojo {

    // ------------------------------ FIELDS ------------------------------

    /**
     * The artifact to display the latest version found. Must be provided as groupId:artifactId, groupId:artifactId:type or groupId:artifactId:type:classifier
     * If no scope is provided jar is used.
     *
     * @since 2.9
     */
    @Parameter(property = "artifact", required = true)
    private final String artifact = null;

    /**
     * The groupId to check for the latest version
     */
    private String groupId = null;

    /**
     * The type of the artifact to check for the latest version
     */
    private String type;

    /**
     * The classifier of the artifact to check for the latest version
     */
    private String classifier = null;

    /**
     * The artifcatId to check for the latest version
     */
    private String artifactId = null;

    @Inject
    public DisplayLatestVersionMojo(
            RepositorySystem repositorySystem,
            org.eclipse.aether.RepositorySystem aetherRepositorySystem,
            Map<String, Wagon> wagonMap,
            Map<String, ChangeRecorder> changeRecorders) {
        super(repositorySystem, aetherRepositorySystem, wagonMap, changeRecorders);
    }

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {

        setFromArtifact();

        logInit();
        try {
            Artifact artifact = getHelper().createDependencyArtifact(groupId, artifactId, "", type, classifier, null);

            ArtifactVersion latestVersion = findLatestVersion(artifact, null, allowSnapshots, false);
            if (null != latestVersion) {
                logLine(false, format("Latest version found - %s:%s:%s:%s", groupId, artifactId, type, latestVersion));
            } else {
                logLine(false, format("Artifact %s:%s:%s not found", groupId, artifactId, type));
            }
        } catch (VersionRetrievalException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void update(ModifiedPomXMLEventReader pom) {}

    private void setFromArtifact() {
        if (null != this.artifact) {
            String[] parts = artifact.split(":");
            groupId = ArrayUtils.get(parts, 0) != null ? parts[0] : null;
            artifactId = ArrayUtils.get(parts, 1) != null ? parts[1] : null;
            type = ArrayUtils.get(parts, 2) != null ? parts[2] : "jar";
            classifier = ArrayUtils.get(parts, 3) != null ? parts[3] : null;
        }
    }
}
