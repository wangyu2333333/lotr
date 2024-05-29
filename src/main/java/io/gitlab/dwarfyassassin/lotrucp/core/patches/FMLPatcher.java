package io.gitlab.dwarfyassassin.lotrucp.core.patches;

import org.objectweb.asm.tree.*;

import io.gitlab.dwarfyassassin.lotrucp.core.UCPCoreMod;
import io.gitlab.dwarfyassassin.lotrucp.core.patches.base.Patcher;
import io.gitlab.dwarfyassassin.lotrucp.core.utils.ASMUtils;

public class FMLPatcher extends Patcher {
	public FMLPatcher() {
		super("FML");
		classes.put("cpw.mods.fml.common.LoadController", new Patcher.ConsumerImplBecauseNoLambdas<ClassNode>() {

			@Override
			public void accept(ClassNode node) {
				FMLPatcher.this.patchLoadController(node);
			}
		});
	}

	public void patchLoadController(ClassNode classNode) {
		MethodNode method = ASMUtils.findMethod(classNode, "buildModList", "(Lcpw/mods/fml/common/event/FMLLoadEvent;)V");
		if (method == null) {
			return;
		}
		method.instructions.insert(new MethodInsnNode(184, "io/gitlab/dwarfyassassin/lotrucp/core/hooks/PreMCHooks", "postFMLLoad", "()V", false));
		UCPCoreMod.log.info("Patched the FML load controller.");
	}

}
