#!/bin/bash

cd `dirname $0`
echo "Tagging"
version=$(cat ../src/main/resources/version.properties)
echo "v. $version"
git tag "$version"

echo "Pushing...."
git push
git push --tags
echo "Done.  Have phun!"