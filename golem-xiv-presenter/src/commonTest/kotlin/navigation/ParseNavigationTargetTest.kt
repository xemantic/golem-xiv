/*
 * Copyright (c) 2025. Kazimierz Pogoda / Xemantic. All rights reserved.
 *
 * This code is part of the "golem-xiv" project, a cognitive AI agent.
 * Unauthorized reproduction or distribution is prohibited.
 */

package com.xemantic.ai.golem.presenter.navigation

import com.xemantic.kotlin.test.be
import com.xemantic.kotlin.test.have
import com.xemantic.kotlin.test.should
import kotlin.test.Test

class ParseNavigationTargetTest {

    @Test
    fun `should parse cognition target`() {
        parseNavigationTarget("/cognition/42") should {
            be<Navigation.Target.Cognition>()
            have(id == 42L)
        }
    }

}
