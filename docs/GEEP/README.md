# GEEP - Golem Evolution and Enhancement Process

This directory contains proposals for [Golem XIV](https://github.com/xemantic/golem-xiv), including
draft design notes and discussions for in-progress proposals as well as
the design documentation on changes that were already implemented.

The proposals themselves are colloquially referred to as GEEPs.
They cover the cognitive architecture, GolemScript, Knowledge Graph memory, APIs, etc.

## GEEP document lifecycle

**1. The proposal is published.**
Every new proposal gets a GEEP number (previous proposal number + 1).
The Markdown file is prefixed with the `GEEP-xxxx-` number and merged to the `main` branch.

**2. Public GEEP review stage.**
At this stage, we invite the community to read the proposal and share their opinion
on the [Xemantic Discord server](https://discord.gg/vQktqqN2Vn).

**3. The final decision on the proposal.**
The proposal is either Accepted, Rejected, or requires further refinement.

The up-to-date status of each proposal must be recorded in the GEEP document header.
Generally we use the following statuses:
- `Draft` - initial proposal, not yet ready for public review
- `Public discussion` - open for community feedback
- `In progress` - accepted and being implemented
- `Experimental` - implemented but subject to change
- `Stable` - fully implemented and stable
- `Declined` - proposal was rejected
- `Superseded by GEEP-xxxx` - replaced by another proposal

## Contributing use-cases and enhancement proposals

If you have a use case that is not covered or have a specific enhancement in mind,
please file a [GitHub Issue](https://github.com/xemantic/golem-xiv/issues).

Contributing **real-life use-cases** is the most valuable piece of feedback from the community.

## Contributing to existing GEEPs

* For in-progress GEEPs, please keep discussions in [Xemantic Discord server](https://discord.gg/vQktqqN2Vn).
* If you find problems with the _text_ or have corrections for merged GEEPs, feel free to create a
  pull request with the proposed correction.

## Contributing design proposals (GEEPs)

If you'd like to propose a new GEEP, please start by opening a GitHub Issue to discuss the idea.
Once there's agreement on the direction, you can submit a Pull Request with the proposal document.
