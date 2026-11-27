"""Cria um arquivo SQLite novo com a estrutura e a carga opcional."""

import argparse
import sqlite3
from contextlib import closing
from pathlib import Path


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("arquivo", nargs="?", type=Path, default=Path("dieta_facil.db"))
    parser.add_argument("--somente-estrutura", action="store_true")
    args = parser.parse_args()
    pasta = Path(__file__).resolve().parent

    if sqlite3.sqlite_version_info < (3, 37):
        parser.error("É necessário SQLite 3.37 ou mais recente.")

    with closing(sqlite3.connect(":memory:")) as banco:
        banco.executescript((pasta / "01_ddl_estrutura.sql").read_text(encoding="utf-8-sig"))
        if not args.somente_estrutura:
            banco.executescript((pasta / "02_dml_carga_inicial.sql").read_text(encoding="utf-8-sig"))
        banco.executescript((pasta / "03_programacao_avancada.sql").read_text(encoding="utf-8-sig"))
        if banco.execute("PRAGMA integrity_check").fetchall() != [("ok",)]:
            raise RuntimeError("Falha na verificação de integridade.")
        if banco.execute("PRAGMA foreign_key_check").fetchall():
            raise RuntimeError("Há referências inválidas no banco.")
        imagem = banco.serialize()

    try:
        with args.arquivo.open("xb") as arquivo:
            arquivo.write(imagem)
    except FileExistsError:
        parser.error(f"O arquivo já existe: {args.arquivo}. Escolha outro nome.")
    print(f"SQLite {sqlite3.sqlite_version} | {args.arquivo.resolve()}")


if __name__ == "__main__":
    main()
