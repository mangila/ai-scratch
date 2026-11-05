# {{name}}

{{name}} has the national Pokedex number {{id}}.

## Traits
{{#is_baby}}
- This Pokémon is a baby.
  {{/is_baby}}
  {{^is_baby}}
- This Pokémon is not a baby.
  {{/is_baby}}

{{#is_legendary}}
- **Status:** Legendary Pokémon
  {{/is_legendary}}

{{#is_mythical}}
- **Status:** Mythical Pokémon
  {{/is_mythical}}

## Description

{{description}}