#!/bin/bash
set -e

git config --global user.email "Jacoby6000@gmail.com  "
git config --global user.name "Jacoby6000"
git config --global push.default simple

sbt docs/publishMicrosite
