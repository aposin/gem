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


# This file is used by GEM to generate the git-hooks on every cloned
# repository - this master scripts should not be removed!
# Generated hooks are client-side ones (e.g., pre-commit,
# pre-rebase, pre-push).

# This template iterates over the ${hookname}.d directory
# and runs every hook that is there, passing any argument

# For example, this file could be copied to the .git/hooks/pre-commit
# to run pre-commit hooks on the .git/hooks/pre-commit.d/ directory

hookname=$(basename $0)
hookdir=$(dirname $0)/${hookname}.d

if [[ -d ${hookdir} ]]; then
	for hook in ${hookdir}/*; do
		$hook "$@"
		rc=$?
		if [ $rc != 0 ]; then
			exit $rc
		fi
	done
fi

exit 0
