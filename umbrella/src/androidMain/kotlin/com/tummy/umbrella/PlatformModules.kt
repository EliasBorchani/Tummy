package com.tummy.umbrella

import org.koin.core.module.Module

// Android-side : le NameProvider est fourni par composeApp's androidAppModule
// (qui dispose du Context Android). Ici on ne déclare rien.
internal actual val platformModules: List<Module> = emptyList()
