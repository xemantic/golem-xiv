# Cognizer

> A cognizer is any entity — biological or artificial — that engages in cognition: the processes of perceiving, representing, reasoning about, and responding to its environment. The term is used primarily in philosophy of mind, cognitive science, and AI theory to refer abstractly to the subject or agent of cognitive processes, without presupposing whether that subject is human, animal, or machine.

## Why cognizer?

In classical cognitive science, a cognizer is typically understood as a system that maintains internal representations and performs computations over them. In more embodied or enactivist traditions, the definition shifts toward any system that actively makes sense of its world through sensorimotor coupling — placing less emphasis on internal representation and more on the dynamic relationship between the system and its environment.

The term is useful precisely because of its neutrality: saying "cognizer" rather than "thinker" or "mind" avoids importing assumptions about consciousness, intentionality, or biological substrate. This makes it particularly relevant in AI contexts, where the question of whether a system truly cognizes or merely simulates cognition remains open and contested.

Golem is funded on the assumption that an output of AI system can be considered not only as generated text (which would be merely a poetry), but can be interpreted as a cognitive process instead, even if the process is "simulated". We are using language in the performative function, through [intents](intent.md) allowing the agent to impact particular environment, which state it is being modeled during inference.

> [!NOTE]
> Golem can use various LLMs, and possibly other model architectures in the future, as agents of its cognitive processes. This is where practical function of "cognizer" comes into play. Each [enunaciation](enunciation.md) originating from AI system, and perpetuating Golem's "self", specifies all the parameters used for performing particular cognition, which allows precise metacognitive analysis and future self-improvement loops. **In consequence a cognitive process can unfold (as generative continuation) using various cognizers on the way.

## Supported cognizers

- [Anthropic](../golem-xiv-cognizer-anthropic)
   - Claude models hosted by Anhtropic and other providers
   - Kimi models
- [Dashscope](../golem-xiv-cognizer-dashscope) (currently disabled)
   - Alibaba QWEN models
- OpenAI (in the making)
   - GPT
   - Mistral
