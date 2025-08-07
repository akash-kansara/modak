package io.github.akashkansara.modak.core.testbed

import io.github.akashkansara.modak.api.GroupSequence

interface BranchGroup

interface RegionalBranchGroup : BranchGroup

interface SloughGroup

interface SwindonGroup

@GroupSequence(value = [SloughGroup::class, SwindonGroup::class])
interface CorporateGroup

@GroupSequence(value = [CorporateGroup::class])
interface InvalidGroupSequence

@GroupSequence(value = [InvalidNestedGroup::class])
interface InvalidNestedGroup
