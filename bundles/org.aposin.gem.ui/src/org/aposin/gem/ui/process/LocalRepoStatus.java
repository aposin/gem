/**
 * Copyright 2020 Association for the promotion of open-source insurance software and for the establishment of open interface standards in the insurance industry (Verein zur FÃ¶rderung quelloffener Versicherungssoftware und Etablierung offener Schnittstellenstandards in der Versicherungsbranche)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.aposin.gem.ui.process;

import java.util.function.Function;
import java.util.function.Supplier;
import org.aposin.gem.core.api.model.IRepository;
import org.aposin.gem.core.api.model.IWorktreeDefinition;
import org.aposin.gem.core.api.workflow.IFeatureBranch;
import org.aposin.gem.ui.message.Messages;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.swt.graphics.Image;

/**
 * Repository status enumeration.
 */
public enum LocalRepoStatus {

    /**
     * Placeholder for fetching status.
     */
    PLACEHOLDER(m -> m.localRepoStatusPlaceholder_label_uiString, //
            () -> FieldDecorationRegistry.getDefault() // info
                    .getFieldDecoration(FieldDecorationRegistry.DEC_INFORMATION).getImage()),
    /**
     * Repository information is not available.
     */
    NOT_AVAILABLE(m -> m.localRepoStatusPlaceholder_label_uiString, //
            () -> FieldDecorationRegistry.getDefault() // error quick-fix
                    .getFieldDecoration(FieldDecorationRegistry.DEC_ERROR_QUICKFIX).getImage()),
    /**
     * Repository is ready (no other status).
     */
    READY(m -> m.localRepoStatusReady_label_uiString, //
            () -> null), // no image
    /**
     * When {@link IRepository#isCloned()} is {@code false}.
     */
    REQUIRES_CLONE(m -> m.localRepoStatusRequiresClone_label_uiString, //
            () -> FieldDecorationRegistry.getDefault() // error quick-fix
                    .getFieldDecoration(FieldDecorationRegistry.DEC_ERROR_QUICKFIX).getImage()),

    /**
     * When {@link IWorktreeDefinition#isAdded()} is {@code false}.
     */
    REQUIRES_WORKTREE_REMOVAL(m -> m.localRepoStatusRequiresWorktreeRemoval_label_uiString, //
            () -> FieldDecorationRegistry.getDefault() // error quick-fix
                    .getFieldDecoration(FieldDecorationRegistry.DEC_ERROR_QUICKFIX).getImage()),

    /**
     * When {@link IWorktreeDefinition#isAdded()} is {@code false}.
     */
    REQUIRES_WORKTREE(m -> m.localRepoStatusRequiresWorktree_label_uiString, //
            () -> FieldDecorationRegistry.getDefault() // error quick-fix
                    .getFieldDecoration(FieldDecorationRegistry.DEC_ERROR_QUICKFIX).getImage()),

    /**
     * When {@link IWorktreeDefinition#getBranch()} != {@link IFeatureBranch#getCheckoutBranch()}.
     */
    REQUIRES_FB_CHECKOUT(m -> m.localRepoStatusRequiresFbCheckout_label_uiString, //
            () -> FieldDecorationRegistry.getDefault() // error quick-fix
                    .getFieldDecoration(FieldDecorationRegistry.DEC_ERROR_QUICKFIX).getImage());

    private final Function<Messages, String> stringFunction;
    private final Supplier<Image> imageSupplier;

    private LocalRepoStatus(final Function<Messages, String> stringFunction,
            final Supplier<Image> imageSupplier) {
        this.stringFunction = stringFunction;
        this.imageSupplier = imageSupplier;
    }


    /***
     * Gets the status string.
     * 
     * @param messages localized messages.
     * 
     * @return status string.
     */
    public String getStatusString(final Messages messages) {
        return stringFunction.apply(messages);
    }

    /***
     * Gets the status image.
     * 
     * @param messages localized messages.
     * 
     * @return status image; null if not required.
     */
    public Image getStatusDecoratorImage() {
        return imageSupplier.get();
    }

}
