#!/bin/bash

die() {
    echo ""
    echo "ERROR: $*"
    local frame=0
    while caller $frame; do
        ((frame++));
    done
    echo ""
    exit 1
}

help() {
	echo "Syntax: $0 <releaseVersion> <developmentVersion>"
	echo "   releaseVersion     - will be suffixed by -MOTORTALK"
	echo "   developmentVersion - will be suffixed by -SNAPSHOT"
	exit ${1}
}

if [ -z "$2" ]; then
	help 1
fi

releaseVersion=${1}-MOTORTALK
developmentVersion=${2}-SNAPHOT

echo "Are you sure you running this command not an a clone of the git branch you active developing on?"
echo -n "If you proceed all files in current directory will be reverted to HEAD state of the git repository [y/N]? "
read REPLY
if [ "$REPLY" != "y" ]; then
    echo "Aborted - Bye!"
    exit 1
fi

echo ""
echo "1/5) Revert all changes of current directory..."
git reset --hard HEAD || die "Could not revert all changes."
echo ""
echo "2/5) Git pull of current directory..."
git pull -v --progress "origin" || die "Could not pull new changes."

echo ""
echo "3/5) Prepare release of version ${releaseVersion}..."
mvn "-DreleaseVersion=${releaseVersion}" "-DdevelopmentVersion=${developmentVersion}" "-Dtag=version-${releaseVersion}" release:prepare || die "Could not prepare release."

echo ""
echo "4/5) Perform release of version ${releaseVersion}..."
mvn "-DaltDeploymentRepository=motortalk-forksOnGithub-releases::default::dav:http://repo.motortalk.biz/forksOnGithub-releases" release:preform || die "Could not perform release."

echo ""
echo "5/5) Push changes of release back to GIT..."
git push origin || die "Could not push the result of the release to back to GIT. Make the push manually by your self!"

echo ""
echo "DONE - Bye!"
