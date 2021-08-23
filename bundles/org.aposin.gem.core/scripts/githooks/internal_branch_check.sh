#!/bin/sh
#
# Copyright 2020 Association for the promotion of open-source insurance software and for the establishment of open interface standards in the insurance industry (Verein zur Foerderung quelloffener Versicherungssoftware und Etablierung offener Schnittstellenstandards in der Versicherungsbranche)
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#


# Hook to fails if the hook is run on the internal GEM branch
# This hook should be installed on every repository for:
# pre-commit, pre-push, pre-rebase 

# assumes that IEnvironment#INTERNAL_BRANCH_PREFIX is used as
# a prefix for the environments
internal_branch_prefix=internal/GEM/*

# get the current branch
current_branch=$(git branch --show-current)
if [[ $current_branch == $internal_branch_prefix ]]; then
	echo "Action is not allowed in the internal branch (prefixed with '${internal_branch_prefix}')" >&2
	exit 1
fi
