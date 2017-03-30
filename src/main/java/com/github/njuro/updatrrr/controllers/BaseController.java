package com.github.njuro.updatrrr.controllers;

import com.github.njuro.updatrrr.UpdatRRR;

/**
 * Base class for com.github.njuro.updatrrr.controllers
 *
 * @author njuro
 */
public abstract class BaseController {

    protected static UpdatRRR manager;

    public static void initManager(UpdatRRR manager) {
        if (BaseController.manager == null) {
            BaseController.manager = manager;
        }
    }
}
